package com.tripply.Auth.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tripply.Auth.constants.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthenticationResponse {

    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("refresh_token")
    private String refreshToken;

    @JsonProperty("expires_in")
    private long expiresIn;

    @JsonProperty("role")
    private UserRole role;

    public AuthenticationResponse(String accessToken, long expiresIn) {
        super();
        this.accessToken = accessToken;
        this.expiresIn = expiresIn;
    }
}
