package com.tripply.Auth.service;

import com.tripply.Auth.model.request.LoginRequest;
import com.tripply.Auth.model.response.AuthenticationResponse;
import com.tripply.Auth.model.ResponseModel;

public interface AuthService {

    ResponseModel<AuthenticationResponse> authenticateUser(LoginRequest request);

    ResponseModel<String> blockToken(String jwt);

    boolean checkTokenIsBlocked(String jwt);

    ResponseModel<AuthenticationResponse> getRefreshToken(String jwt);
}
