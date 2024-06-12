package com.tripply.Auth.service.Impl;

import com.tripply.Auth.constants.ErrorConstant;
import com.tripply.Auth.entity.BlackListToken;
import com.tripply.Auth.entity.PasswordResetToken;
import com.tripply.Auth.entity.User;
import com.tripply.Auth.exception.BadCredentialsException;
import com.tripply.Auth.exception.BadRequestException;
import com.tripply.Auth.exception.FailToSaveException;
import com.tripply.Auth.exception.RecordNotFoundException;
import com.tripply.Auth.exception.ServiceCommunicationException;
import com.tripply.Auth.repository.PasswordResetTokenRepository;
import com.tripply.Auth.repository.UserRepository;
import com.tripply.Auth.exception.*;
import com.tripply.Auth.model.ResponseModel;
import com.tripply.Auth.model.request.LoginRequest;
import com.tripply.Auth.model.response.AuthenticationResponse;
import com.tripply.Auth.repository.BlackListTokenRepository;
import com.tripply.Auth.service.AuthService;
import com.tripply.Auth.service.WebClientService;
import com.tripply.Auth.util.JwtUtil;
import com.tripply.Auth.util.RandomTokenGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

import java.util.Date;
import java.util.Optional;

import static com.tripply.Auth.constants.AuthConstants.DUMMY_TOKEN;
import static com.tripply.Auth.constants.AuthConstants.SEND_FORGET_PASSWORD_EMAIL;

@Service
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final BlackListTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final WebClientService webClientService;

    public AuthServiceImpl(JwtUtil jwtUtil, UserRepository userRepository, BlackListTokenRepository tokenRepository, PasswordEncoder passwordEncoder, PasswordResetTokenRepository passwordResetTokenRepository, WebClientService webClientService) {
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.webClientService = webClientService;
    }

    @Value("${application.notification.base-url}")
    private String notificationBaseUrl;

    @Override
    public ResponseModel<AuthenticationResponse> authenticateUser(LoginRequest loginRequest) {
        log.info("AuthService: authenticateUser() started with username -> {}", loginRequest.getEmail());
        ResponseModel<AuthenticationResponse> response = new ResponseModel<>();
        Optional<User> userDetails = userRepository.findByEmail(loginRequest.getEmail());
        if (userDetails.isEmpty()) {
            throw new RecordNotFoundException("User not found");
        }

        User user = userDetails.get();
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new BadCredentialsException(ErrorConstant.ER004.getErrorDescription());
        }

        if (!user.isEnabled()) {
            throw new UnAuthorizedException("Please verify your Account");
        }

        final String token = jwtUtil.generateToken(user);
        long expirationDate = jwtUtil.getExpirationTime();
        final String refreshToken = jwtUtil.generateRefreshToken(user);
        AuthenticationResponse authResponse = new AuthenticationResponse(token, expirationDate, refreshToken);
        authResponse.setRole(user.getRole());
        response.setData(authResponse);
        response.setMessage("Retrieved token details successfully.");
        response.setStatus(HttpStatus.OK);

        log.info("AuthService: authenticateUser() ended with username -> {}", loginRequest.getEmail());
        return response;
    }

    @Override
    public ResponseModel<String> blockToken(String jwt) {
        log.info("AuthService: blockToken() started with jwt -> {}", jwt);
        BlackListToken token = tokenRepository.findByTokenValue(jwt);
        if (token != null) {
            throw new BadRequestException("Token is already blocked");
        }
        BlackListToken newToken = new BlackListToken();
        newToken.setTokenValue(jwt);
        newToken.setExpirationTime(0);
        try {
            tokenRepository.save(newToken);
            log.info("token saved");
        } catch (FailToSaveException e) {
            throw new FailToSaveException("Failed to save block token");
        }
        ResponseModel<String> response = new ResponseModel<>();
        response.setStatus(HttpStatus.CREATED);
        response.setMessage("Log out successfully");
        log.info("AuthService: blockToken() ended with jwt -> {}", jwt);
        return response;
    }
    @Override
    public ResponseModel<String> forgotPassword(String email) {
        log.info("AuthService : forgotPassword() started with email -> {}" , email);
        ResponseModel<String> response = new ResponseModel<>();
        User user = userRepository.findByEmail(email).orElseThrow(
                () -> {
                    throw new RecordNotFoundException("User not found");
                }
        );
        PasswordResetToken passwordResetToken = new PasswordResetToken();
        passwordResetToken.setUserId(user.getId());
        passwordResetToken.setToken(RandomTokenGenerator.generateToken());
        passwordResetToken.setCreatedOn(new Date());
        passwordResetTokenRepository.save(passwordResetToken);

        try {
            return webClientService.postWithParameterizedTypeReference(notificationBaseUrl + SEND_FORGET_PASSWORD_EMAIL,
                    passwordResetToken,
                    new ParameterizedTypeReference<>() {
                    },
                    DUMMY_TOKEN);
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error("Error while sending forgot password email", e);
            throw new ServiceCommunicationException("Error occurred while calling notification service");
        } catch (ResourceAccessException e) {
            log.error("Network error while sending forgot password email", e);
            throw new ServiceCommunicationException("Network error occurred while calling to notification service");
        }
    }

    @Override
    public ResponseModel<String> resetPassword(String password, String token) {
        log.info("AuthService : resetPassword() started");
        ResponseModel<String> response = new ResponseModel<>();
        PasswordResetToken passwordResetToken = passwordResetTokenRepository.findByToken(token);
        if (passwordResetToken == null){
            response.setStatus(HttpStatus.FORBIDDEN);
            response.setMessage("Token does not exist");
        } else if (passwordResetToken.isExpired()){
            response.setStatus(HttpStatus.FORBIDDEN);
            response.setMessage("Token expired.");
        } else {
            try{
                User user = userRepository.findById(passwordResetToken.getId()).orElseThrow(
                        () -> {
                            throw new RecordNotFoundException("User not found");
                        }
                );
                user.setPassword(password);
                userRepository.save(user);
                passwordResetTokenRepository.delete(passwordResetToken);
                response.setStatus(HttpStatus.OK);
                response.setMessage("Password updated successfully.");
            }
            catch (ResourceAccessException e){
                throw new RecordNotFoundException("User not found");
            }
        }
        return response;
    }

    private boolean isCorrectPassword(String encodedPass, String inputPass) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        return encoder.matches(inputPass, encodedPass);
    }

    @Override
    public boolean checkTokenIsBlocked(String jwt) {
        log.info("AuthService: checkTokenIsBlocked() started with jwt -> {}", jwt);
        BlackListToken token = tokenRepository.findByTokenValue(jwt);
        if (token == null) {
            return false;
        }
        log.info("AuthService: checkTokenIsBlocked() ended with token -> {}", jwt);
        return true;
    }

}
