package com.tripply.Auth.model.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserRequest {
    @NotNull(message = "User email can not be null")
    private String sentToEmail;
    @NotNull(message = "User name can not be null")
    private String sendToName;
}
