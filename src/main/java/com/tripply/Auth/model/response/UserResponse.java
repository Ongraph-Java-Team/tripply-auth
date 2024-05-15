package com.tripply.Auth.model.response;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserResponse {

    private String firstName;

    private String lastName;

    private String email;

    private String phoneNumber;

    private String countryCode;

}
