package com.tripply.Auth.controller;

import com.tripply.Auth.dto.RoleDto;
import com.tripply.Auth.dto.UserDto;
import com.tripply.Auth.model.ResponseModel;
import com.tripply.Auth.model.request.InviteRequest;
import com.tripply.Auth.model.response.UserResponse;
import com.tripply.Auth.service.UserService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@Slf4j
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping(value = "/register", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public  ResponseEntity<ResponseModel<String>> registerUser(@Valid @RequestBody UserDto userDto) {
        log.info("Endpoint: Registering user: {}", userDto);
        ResponseModel<String> response = userService.saveUser(userDto);
        log.info("Endpoint: Registered user: {}", userDto);
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/createRole", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public  ResponseEntity<ResponseModel<String>> createRole(@Valid @RequestBody RoleDto roleDto) {
        log.info("Endpoint: Creating role: {}", roleDto);
        ResponseModel<String> response = userService.saveRole(roleDto);
        log.info("Endpoint: Created role: {}", roleDto);
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/register/client", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ResponseModel<String>> registerClient(@Valid @RequestBody InviteRequest inviteRequest) {
        log.info("Endpoint: Registering client {}", inviteRequest);
        ResponseModel<String> response =  userService.registerClient(inviteRequest);
        log.info("Endpoint: Registered client {}", inviteRequest);
        return ResponseEntity.ok(response);
    }

    @GetMapping(value = "/{id}")
    public ResponseEntity<ResponseModel<UserResponse>> getUserById(@PathVariable UUID id) {
        log.info("Endpoint: Getting user {}", id);
        ResponseModel<UserResponse> response = userService.getUserById(id);
        log.info("Endpoint: Got User {}", id);
        return ResponseEntity.ok(response);
    }
}
