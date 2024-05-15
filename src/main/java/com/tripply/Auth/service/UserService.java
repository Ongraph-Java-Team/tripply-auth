package com.tripply.Auth.service;

import com.tripply.Auth.dto.RoleDto;
import com.tripply.Auth.dto.UserDto;
import com.tripply.Auth.model.ResponseModel;
import com.tripply.Auth.model.request.InviteRequest;
import com.tripply.Auth.model.response.UserResponse;

import java.util.UUID;

public interface UserService {

    ResponseModel<String> saveUser(UserDto userDto);

    ResponseModel<String> saveRole(RoleDto roleDto);

    ResponseModel<String> registerClient(InviteRequest inviteRequest);

    ResponseModel<UserResponse> getUserById(UUID id);
}
