package com.tripply.Auth.controller;

import com.tripply.Auth.dto.RoleDto;
import com.tripply.Auth.dto.UserDto;
import com.tripply.Auth.model.ResponseModel;
import com.tripply.Auth.model.request.InviteRequest;
import com.tripply.Auth.model.response.UserResponse;
import com.tripply.Auth.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
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

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "Register an user",
            description = "This API will register the user, if user email already exist it will throw User already exists message.")
    @PostMapping(value = "/register", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public  ResponseEntity<ResponseModel<String>> registerUser(@Valid @RequestBody UserDto userDto) {
        log.info("Endpoint: Registering user: {}", userDto);
        ResponseModel<String> response = userService.saveUser(userDto);
        log.info("Endpoint: Registered user: {}", userDto);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Create a role",
            description = "This API will create a role with name, if role already exist it will throw Role already exists message.")
    @PostMapping(value = "/create-role", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public  ResponseEntity<ResponseModel<String>> createRole(@Valid @RequestBody RoleDto roleDto) {
        log.info("Endpoint: Creating role: {}", roleDto);
        ResponseModel<String> response = userService.saveRole(roleDto);
        log.info("Endpoint: Created role: {}", roleDto);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Register a client user",
            description = "This API will register a client user with inviteeId and password details.")
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
