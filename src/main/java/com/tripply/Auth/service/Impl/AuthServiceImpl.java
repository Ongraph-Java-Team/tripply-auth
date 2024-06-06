package com.tripply.Auth.service.Impl;

import com.tripply.Auth.constants.ErrorConstant;
import com.tripply.Auth.entity.BlackListToken;
import com.tripply.Auth.entity.User;
import com.tripply.Auth.exception.*;
import com.tripply.Auth.model.ResponseModel;
import com.tripply.Auth.model.request.LoginRequest;
import com.tripply.Auth.model.response.AuthenticationResponse;
import com.tripply.Auth.repository.BlackListTokenRepository;
import com.tripply.Auth.repository.UserRepository;
import com.tripply.Auth.service.AuthService;
import com.tripply.Auth.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final BlackListTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthServiceImpl(JwtUtil jwtUtil, UserRepository userRepository, BlackListTokenRepository tokenRepository, PasswordEncoder passwordEncoder) {
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
    }

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
    public boolean checkTokenIsBlocked(String jwt) {
        log.info("AuthService: checkTokenIsBlocked() started with jwt -> {}", jwt);
        BlackListToken token = tokenRepository.findByTokenValue(jwt);
        if (token == null) {
            return false;
        }
        log.info("AuthService: checkTokenIsBlocked() ended with token -> {}", jwt);
        return true;
    }

    @Override
    public ResponseModel<AuthenticationResponse> getRefreshToken(String refreshToken) {
        log.info("AuthService: getRefreshToken() started with jwt -> {}", refreshToken);
        if(refreshToken==null || jwtUtil.isTokenExpired(refreshToken)) {
            throw new BadRequestException("Invalid refresh token");
        }
        String email = jwtUtil.extractUsername(refreshToken);
        User user = userRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("User not found"));
        ResponseModel<AuthenticationResponse> response = new ResponseModel<>();
        final String token = jwtUtil.generateToken(user);
        long expirationDate = jwtUtil.getExpirationTime();
        AuthenticationResponse authResponse = new AuthenticationResponse(token, expirationDate, refreshToken);
        authResponse.setRole(user.getRole());
        response.setData(authResponse);
        response.setMessage("Retrieved token details successfully.");
        response.setStatus(HttpStatus.OK);
        log.info("AuthService: getRefreshToken() ended with token -> {}", refreshToken);
        return response;
    }

}
