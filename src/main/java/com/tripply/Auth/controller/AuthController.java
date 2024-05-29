package com.tripply.Auth.controller;

import com.tripply.Auth.model.request.LoginRequest;
import com.tripply.Auth.model.response.AuthenticationResponse;
import com.tripply.Auth.model.ResponseModel;
import com.tripply.Auth.service.AuthService;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Tag(name = "GET", description = "GET method to get user's token details")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "401", description = "Invalid email or password entered"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ResponseModel<AuthenticationResponse>> login(@Valid @RequestBody LoginRequest request) {
        log.info("Endpoint: login request: {}", request);
        ResponseModel<AuthenticationResponse> response = authService.authenticateUser(request);
        log.info("Endpoint: login response: {}", response);
        return ResponseEntity.ok(response);
    }

    @GetMapping(value = "/logout")
    public ResponseEntity<ResponseModel<String>> logout(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        log.info("Endpoint: logout request: {}", token);
        ResponseModel<String> response = new ResponseModel<>();
        if (token != null && token.startsWith("Bearer ")) {
            String jwt = token.substring(7);
            response = authService.blockToken(jwt);
        }
        log.info("Endpoint: logout response");
        return ResponseEntity.ok(response);
    }
}
