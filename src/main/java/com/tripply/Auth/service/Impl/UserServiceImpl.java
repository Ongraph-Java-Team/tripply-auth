package com.tripply.Auth.service.Impl;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.tripply.Auth.config.WebClientService;
import com.tripply.Auth.constants.UserRole;
import com.tripply.Auth.dto.*;
import com.tripply.Auth.entity.Role;
import com.tripply.Auth.entity.User;
import com.tripply.Auth.exception.BadRequestException;
import com.tripply.Auth.exception.FailToSaveException;
import com.tripply.Auth.exception.RecordNotFoundException;
import com.tripply.Auth.exception.ServiceCommunicationException;
import com.tripply.Auth.model.request.InviteRequest;
import com.tripply.Auth.model.request.UserRequest;
import com.tripply.Auth.model.response.UserResponse;
import com.tripply.Auth.repository.RoleRepository;
import com.tripply.Auth.repository.UserRepository;
import com.tripply.Auth.model.ResponseModel;
import com.tripply.Auth.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.*;

import static com.tripply.Auth.constants.AuthConstants.*;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private WebClientService webClientService;

    @Value("${application.notification.base-url}")
    private String baseUrl;

    @Value("${application.booking.base-url}")
    private String baseBookingUrl;

    @Value("${application.notification.base-url}")
    private String notificationBaseUrl;

    @Override
    public ResponseModel<String> saveUser(UserDto userDto) {
        log.info("saving user {}", userDto);
        ResponseModel<String> responseModel = new ResponseModel<>();
        Optional<User> existeduser = userRepository.findByEmailOrPhone(userDto.getEmail(), userDto.getPhoneNumber());
        if (existeduser.isPresent()) {
            throw new BadRequestException("This User already exists");
        }
        try {
            User user = User.builder()
                    .firstName(userDto.getFirstName())
                    .lastName(userDto.getLastName())
                    .email(userDto.getEmail())
                    .phoneNumber(userDto.getPhoneNumber())
                    .countryCode(userDto.getCountryCode())
                    .role(UserRole.REGULAR_USER)
                    .build();

            user.setPassword(getEncryptedPassword(userDto.getPassword()));

            user = userRepository.save(user);
            log.info("saved user {}", user);
            responseModel.setMessage("User added successfully");
            responseModel.setStatus(HttpStatus.CREATED);
            sendEmailToUser(user);
        } catch (FailToSaveException e) {
            throw new FailToSaveException("Error while saving user", e);
        }

        return responseModel;
    }

    @Override
    public ResponseModel<String> saveRole(RoleDto roleDto) {
        log.info("saving role {}", roleDto);
        ResponseModel<String> responseModel = new ResponseModel<>();
        try {
            Role role = Role.builder()
                    .roleName(roleDto.getRoleName())
                    .build();

            role = roleRepository.save(role);
            log.info("saved role {}", role);
            responseModel.setMessage("Role added successfully");
            responseModel.setStatus(HttpStatus.CREATED);

        } catch (FailToSaveException e) {
            throw new FailToSaveException("Error occured while saving role", e);
        }

        return responseModel;
    }

    @Override
    public ResponseModel<String> registerClient(InviteRequest inviteRequest) {
        log.info("registering client: {}", inviteRequest);
        ResponseModel<String> response = new ResponseModel<>();

        try {
            log.info("Getting invitation data: {}", inviteRequest);
            JsonObject invitationResponse = getInvitationDetails(inviteRequest);
            if (!Objects.nonNull(invitationResponse)) {
                throw new RecordNotFoundException("Invitation details not found");
            }
            saveUserInDB(invitationResponse, inviteRequest);
            response.setMessage("Hotel onboarded successfully");
            response.setStatus(HttpStatus.CREATED);
        } catch (FailToSaveException e) {
            log.error("An error occurred while creating client: {}", e.getMessage(), e);
            throw new FailToSaveException("An error occurred while creating client", e);
        }
        return response;
    }


    private JsonObject getInvitationDetails(InviteRequest inviteRequest) {
        log.info("Get invite details by inviteId: {}", inviteRequest.getInviteId());
        JsonObject responseData = null;
        try {
            String inviteServiceUri = UriComponentsBuilder
                    .fromHttpUrl(baseUrl + GET_NOTIFICATION_URL + "/" + inviteRequest.getInviteId())
                    .buildAndExpand(inviteRequest.getInviteId())
                    .toUriString();

            WebClient.ResponseSpec responseSpec = WebClient.create()
                    .get()
                    .uri(inviteServiceUri)
                    .retrieve();

            String responseBody = responseSpec.bodyToMono(String.class).block();
            if (responseBody != null) {
                JsonElement jsonElement = JsonParser.parseString(responseBody);
                if (jsonElement.isJsonObject()) {
                    JsonObject jsonObject = jsonElement.getAsJsonObject();
                    JsonElement dataElement = jsonObject.get("data");
                    if (dataElement != null && dataElement.isJsonObject()) {
                        responseData = dataElement.getAsJsonObject();
                    }
                }
            }
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error("Error while calling notification service", e);
            throw new ServiceCommunicationException("Error occurred while calling notification service");
        } catch (ResourceAccessException e) {
            log.error("Network error while getting data from notification service", e);
            throw new ServiceCommunicationException("Network error occurred while calling to notification service");
        }
        return responseData;
    }

    private void saveUserInDB(JsonObject invitationResponse, InviteRequest inviteRequest) {
        JsonObject managerDetails = invitationResponse.getAsJsonObject("hotelRequest")
                .getAsJsonObject("managerDetails");

        if (!Objects.nonNull(managerDetails)) {
            throw new RecordNotFoundException("Manager details not found");
        }
        User user = new User();
        user.setFirstName(managerDetails.get("firstName").getAsString());
        user.setLastName(managerDetails.get("lastName").getAsString());
        user.setEmail(managerDetails.get("email").getAsString());
        user.setCountryCode(managerDetails.get("countryCode").getAsString());
        user.setPhoneNumber(managerDetails.get("phoneNumber").getAsString());
        user.setPassword(getEncryptedPassword(inviteRequest.getPassword()));
        user.setRole(UserRole.CLIENT_ADMIN);
        log.info("saving user.. ");

        Optional<User> userExists = userRepository.findByEmailOrPhone(user.getEmail(), user.getPhoneNumber());
        if (userExists.isPresent()) {
            throw new BadRequestException("This User already exists");
        }
        try {
            userRepository.save(user);
        }  catch (FailToSaveException e) {
            throw new FailToSaveException("Failed to save user", e);
        }
        log.info("saved user {}", user);

        getHotelData(invitationResponse, managerDetails, user);
    }

    private void getHotelData(JsonObject invitationResponse, JsonObject managerDetails, User user) {
        JsonObject hotelRequest = invitationResponse.getAsJsonObject("hotelRequest");
        HotelDto hotelDto = new HotelDto();
        hotelDto.setName(hotelRequest.get("name").getAsString());
        hotelDto.setRegistrationNumber(hotelRequest.get("registrationNumber").getAsString());
        hotelDto.setAddress(hotelRequest.get("address").getAsString());
        hotelDto.setCity(hotelRequest.get("city").getAsString());
        hotelDto.setStateId(hotelRequest.get("stateId").getAsString());
        hotelDto.setCountryId(hotelRequest.get("countryId").getAsString());
        hotelDto.setDescription(hotelRequest.get("description").getAsString());
        hotelDto.setWebsite(hotelRequest.get("website").getAsString());

        JsonObject hotelRequestObject = invitationResponse.getAsJsonObject("hotelRequest");

        JsonArray amenitiesArray = hotelRequestObject.getAsJsonArray("amenities");

        List<Amenity> amenities = new ArrayList<>();
        for (JsonElement amenityElement : amenitiesArray) {
            JsonObject amenityObject = amenityElement.getAsJsonObject();
            String name = amenityObject.get("name").getAsString();
            String description = amenityObject.get("description").getAsString();
            Amenity amenity = new Amenity();
            amenity.setName(name);
            amenity.setDescription(description);
            amenities.add(amenity);
        }
        hotelDto.setAmenities(amenities);

        ManagerDetails managerDetails1 = new ManagerDetails();
        managerDetails1.setUserId(user.getId());
        managerDetails1.setFirstName(managerDetails.get("firstName").getAsString());
        managerDetails1.setLastName(managerDetails.get("lastName").getAsString());
        managerDetails1.setEmail(managerDetails.get("email").getAsString());
        managerDetails1.setCountryCode(managerDetails.get("countryCode").getAsString());
        managerDetails1.setPhoneNumber(managerDetails.get("phoneNumber").getAsString());

        hotelDto.setManagerDetails(managerDetails1);

        log.info("Onboarding hotel: {}", hotelRequest);
        onboardHotel(hotelDto);
    }

    public void onboardHotel(HotelDto hotelDto) {
        try {
            String onboardHotelUri = UriComponentsBuilder
                    .fromHttpUrl(baseBookingUrl + ONBOARD_HOTEL)
                    .toUriString();

            WebClient.create()
                    .post()
                    .uri(onboardHotelUri)
                    .bodyValue(hotelDto)
                    .retrieve()
                    .toBodilessEntity()
                    .block();

            log.info("Hotel onboarded: {}", hotelDto);

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            throw new ServiceCommunicationException("Error occurred while calling booking service");
        } catch (ResourceAccessException e) {
            throw new ServiceCommunicationException("Network error occurred while calling booking service");
        }

    }

    @Override
    public ResponseModel<UserResponse> getUserById(UUID id) {
        Optional<User> user = userRepository.findById(id);
        if (user.isEmpty()) {
            throw new RecordNotFoundException("User not found");
        }
        UserResponse userResponse = new UserResponse();
        userResponse.setFirstName(user.get().getFirstName());
        userResponse.setLastName(user.get().getLastName());
        userResponse.setEmail(user.get().getEmail());
        userResponse.setCountryCode(user.get().getCountryCode());
        userResponse.setPhoneNumber(user.get().getPhoneNumber());

        ResponseModel<UserResponse> responseModel = new ResponseModel<>();
        responseModel.setStatus(HttpStatus.FOUND);
        responseModel.setData(userResponse);
        responseModel.setMessage("User found");
        return responseModel;
    }

    @Override
    public ResponseModel<String> updateUser(String userEmail) {
        Optional<User> userOptional = userRepository.findByEmail(userEmail);
        ResponseModel<String> response = new ResponseModel<>();
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            user.setEnabled(true);
            userRepository.save(user);
            response.setStatus(HttpStatus.OK);
            response.setMessage("User updated successfully");
        } else {
            response.setStatus(HttpStatus.BAD_REQUEST);
            response.setMessage("User not found");
        }
        return response;
    }

    private void sendEmailToUser(User user){
        log.info("Begin sendMail() for the request: {} ", user.getFirstName());
        try {
            UserRequest userRequest = new UserRequest();
            userRequest.setSendToName(user.getFirstName());
            userRequest.setSentToEmail(user.getEmail());
            webClientService.postWithParameterizedTypeReference(notificationBaseUrl + SEND_REGISTRATION_EMAIL_URL,
                    userRequest,
                    new ParameterizedTypeReference<>() {
                    });
        } catch (WebClientResponseException.BadRequest e) {
            log.error("Bad request error while sending email to user", e);
            throw new BadRequestException("Error occurred while sending email. Notification service responded with a bad request.");
        } catch (ResourceAccessException e) {
            log.error("Network error while sending therapist invite", e);
            throw new ServiceCommunicationException("Network error occurred while calling to notification service");
        }
    }

    private String getEncryptedPassword( String password) {
        return passwordEncoder.encode(password);
    }
}
