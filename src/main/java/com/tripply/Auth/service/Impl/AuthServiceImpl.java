package com.tripply.Auth.service.Impl;

import com.tripply.Auth.constants.ErrorConstant;
import com.tripply.Auth.entity.User;
import com.tripply.Auth.exception.BadCredentialsException;
import com.tripply.Auth.exception.RecordNotFoundException;
import com.tripply.Auth.repository.UserRepository;
import com.tripply.Auth.model.request.LoginRequest;
import com.tripply.Auth.model.response.AuthenticationResponse;
import com.tripply.Auth.model.ResponseModel;
import com.tripply.Auth.service.AuthService;
import com.tripply.Auth.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
public class AuthServiceImpl implements AuthService {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    @Override
    public ResponseModel<AuthenticationResponse> authenticateUser(LoginRequest loginRequest) {
        log.info("AuthService: authenticateUser() started with username -> {}", loginRequest.getEmail());
        ResponseModel<AuthenticationResponse> response = new ResponseModel<>();
        Optional<User> userDetails = userRepository.findByEmail(loginRequest.getEmail());
        if (userDetails.isEmpty()) {
            throw new RecordNotFoundException("User not found");
        }

        User user = userDetails.get();
        if (!isCorrectPassword(user.getPassword(), loginRequest.getPassword())) {
            throw new BadCredentialsException(ErrorConstant.ER004.getErrorDescription());
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

    private boolean isCorrectPassword(String encodedPass, String inputPass) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        return encoder.matches(inputPass, encodedPass);
    }
}
