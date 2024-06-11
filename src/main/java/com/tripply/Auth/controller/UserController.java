package com.tripply.Auth.controller;

import java.util.UUID;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tripply.Auth.dto.RoleDto;
import com.tripply.Auth.dto.UserDto;
import com.tripply.Auth.model.ResponseModel;
import com.tripply.Auth.model.request.InviteRequest;
import com.tripply.Auth.model.response.InvitationDetailResponse;
import com.tripply.Auth.model.response.UserResponse;
import com.tripply.Auth.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

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

    @GetMapping(value = "/invitee/{id}")
    @PreAuthorize("hasRole('REGULAR_USER')")
    public ResponseEntity<ResponseModel<InvitationDetailResponse>> getInviteeDetailsById(@PathVariable String id) {
        log.info("Endpoint: Getting user {}", id);
        ResponseModel<InvitationDetailResponse> response = userService.getInvitationDetails(id);
        log.info("Endpoint: Got User {}", id);
        return ResponseEntity.ok(response);
    }

    @Tag(name = "GET", description = "GET method to confirm user account after registration ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/confirm/registration")
    public ResponseEntity<ResponseModel<String>> confirmUserAccount(@RequestParam("inviteeEmail") String userEmail) {
        log.info("Start Endpoint: confirming user account {}", userEmail);
        ResponseModel<String> response = userService.enableUser(userEmail);
        log.info("End Endpoint: confirmed user account {}", userEmail);
        return ResponseEntity.ok(response);
    }
}
