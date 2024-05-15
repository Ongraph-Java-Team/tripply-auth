package com.tripply.Auth.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class LoginRequest {

    @JsonProperty("email")
    @NotNull(message = "Email is required")
    private String email;

    @JsonProperty("password")
    @NotNull(message = "Password is required")
    private String password;

}
