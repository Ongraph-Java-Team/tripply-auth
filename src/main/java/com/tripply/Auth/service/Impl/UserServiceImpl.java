package com.tripply.Auth.service.Impl;

import static com.tripply.Auth.constants.AuthConstants.DUMMY_TOKEN;
import static com.tripply.Auth.constants.AuthConstants.GET_NOTIFICATION_URL;
import static com.tripply.Auth.constants.AuthConstants.ONBOARD_HOTEL;
import static com.tripply.Auth.constants.AuthConstants.SEND_REGISTRATION_EMAIL_URL;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;

import com.tripply.Auth.constants.UserRole;
import com.tripply.Auth.dto.HotelDto;
import com.tripply.Auth.dto.ManagerDetails;
import com.tripply.Auth.dto.RoleDto;
import com.tripply.Auth.dto.UserDto;
import com.tripply.Auth.entity.Role;
import com.tripply.Auth.entity.User;
import com.tripply.Auth.exception.BadRequestException;
import com.tripply.Auth.exception.FailToSaveException;
import com.tripply.Auth.exception.RecordNotFoundException;
import com.tripply.Auth.exception.ServiceCommunicationException;
import com.tripply.Auth.model.ResponseModel;
import com.tripply.Auth.model.request.InviteRequest;
import com.tripply.Auth.model.request.UserRequest;
import com.tripply.Auth.model.response.InvitationDetailResponse;
import com.tripply.Auth.model.response.UserResponse;
import com.tripply.Auth.repository.RoleRepository;
import com.tripply.Auth.repository.UserRepository;
import com.tripply.Auth.service.UserService;
import com.tripply.Auth.service.WebClientService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final WebClientService webClient;

    public UserServiceImpl(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder, WebClientService webClient) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.webClient = webClient;
    }

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
                    .enabled(Boolean.FALSE)
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

    private String getEncryptedPassword( String password) {
        return passwordEncoder.encode(password);
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
            ResponseModel<InvitationDetailResponse> invitationResponse = getInvitationDetails(inviteRequest.getInviteId());
            if (Objects.isNull(invitationResponse)) {
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

    @Override
    public ResponseModel<InvitationDetailResponse> getInvitationDetails(String inviteeId) {
        log.info("Get invite details by inviteId: {}", inviteeId);
        try {
            String inviteServiceUri = UriComponentsBuilder
                    .fromHttpUrl(baseUrl + GET_NOTIFICATION_URL + "/" + inviteeId)
                    .buildAndExpand(inviteeId)
                    .toUriString();
            return webClient.getWithParameterizedTypeReference(inviteServiceUri,
                    new ParameterizedTypeReference<>() {
                    },
                    DUMMY_TOKEN);
        } catch (WebClientResponseException.BadRequest e) {
            log.error("Bad request error while getting invitee details", e);
            throw new BadRequestException("Invitee not found");
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error("Error while sending therapist invite", e);
            throw new ServiceCommunicationException("Error occurred while calling notification service");
        } catch (ResourceAccessException e) {
            log.error("Network error while getting invitee details", e);
            throw new ServiceCommunicationException("Network error occurred while calling to notification service");
        }
    }

    private void saveUserInDB(ResponseModel<InvitationDetailResponse> invitationResponse, InviteRequest inviteRequest) {
        ManagerDetails managerDetails = invitationResponse.getData().getHotelRequest().getManagerDetails();

        if (!Objects.nonNull(managerDetails)) {
            throw new RecordNotFoundException("Manager details not found");
        }
        User user = new User();
        BeanUtils.copyProperties(managerDetails, user);
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

        getHotelData(invitationResponse, user);
    }

    private void getHotelData(ResponseModel<InvitationDetailResponse> invitationResponse, User user) {
        HotelDto hotelRequest = invitationResponse.getData().getHotelRequest();
        hotelRequest.getManagerDetails().setUserId(user.getId());
        log.info("Onboarding hotel: {}", hotelRequest);
        onboardHotel(hotelRequest);
    }

    public void onboardHotel(HotelDto hotelDto) {
        log.info("Calling onboarding service to onboard hotel: {}", hotelDto.getName());
        try {
            String onboardHotelUri = UriComponentsBuilder
                    .fromHttpUrl(baseBookingUrl + ONBOARD_HOTEL)
                    .toUriString();
            webClient.postWithParameterizedTypeReference(onboardHotelUri,
                    hotelDto,
                    new ParameterizedTypeReference<>() {
                    },
                    DUMMY_TOKEN);
            log.info("Hotel onboarded: {}", hotelDto);
        } catch (WebClientResponseException.BadRequest e) {
            log.error("Bad request error while onboarding hotel", e);
            throw new BadRequestException("User already registered or invite already sent");
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error("Error while onboarding hotel", e);
            throw new ServiceCommunicationException("Error occurred while calling onboarding service");
        } catch (ResourceAccessException e) {
            log.error("Network error while onboarding hotel", e);
            throw new ServiceCommunicationException("Network error occurred while calling to onboarding service");
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
    public ResponseModel<String> enableUser(String userEmail) {
        Optional<User> userOptional = userRepository.findByEmail(userEmail);
        if (userOptional.isEmpty()) {
            throw new RecordNotFoundException("User not found");
        }
        ResponseModel<String> response = new ResponseModel<>();
        User user = userOptional.get();
        user.setEnabled(true);
        userRepository.save(user);
        response.setStatus(HttpStatus.OK);
        response.setMessage("User updated successfully");
        return response;
    }

    private void sendEmailToUser(User user){
        log.info("Begin sendMail() for the request: {} ", user.getFirstName());
        try {
            UserRequest userRequest = new UserRequest();
            userRequest.setSendToName(String.join(" ",user.getFirstName(), user.getLastName()));
            userRequest.setSentToEmail(user.getEmail());
            webClient.post(notificationBaseUrl + SEND_REGISTRATION_EMAIL_URL,
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
}
