package com.tripply.Auth.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Request body to get refresh token")
public class RefreshTokenDto {

    @NotBlank(message = "Refresh token can't be left blank")
    String refreshToken;
}
