package com.tripply.Auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@Schema(description = "Request body for creating a new user")
public class UserDto {

    @NotNull(message = "First name is required")
    @Schema(description = "User's first name", example = "John", required = true)
    private String firstName;

    @NotNull(message = "Last name is required")
    @Schema(description = "User's last name", example = "Doe", required = true)
    private String lastName;

    @NotNull(message = "Password is required")
    @Schema(description = "User's password", example = "encrypted@123", required = true)
    private String password;

    @NotNull(message = "Email is required")
    @Schema(description = "User's email", example = "john.doe@gmail.com", required = true)
    private String email;

    private String phoneNumber;

    private String countryCode;
}
