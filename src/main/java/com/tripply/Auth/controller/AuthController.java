package com.tripply.Auth.controller;

import com.tripply.Auth.model.request.LoginRequest;
import com.tripply.Auth.model.response.AuthenticationResponse;
import com.tripply.Auth.model.ResponseModel;
import com.tripply.Auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ResponseModel<AuthenticationResponse>> login(@Valid @RequestBody LoginRequest request) {
        log.info("Endpoint: login request: {}", request);
        ResponseModel<AuthenticationResponse> response = authService.authenticateUser(request);
        log.info("Endpoint: login response: {}", response);
        return ResponseEntity.ok(response);
    }
}
