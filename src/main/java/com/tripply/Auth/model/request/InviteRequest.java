package com.tripply.Auth.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Schema(description = "Request body for creating a new client user")
public class InviteRequest {

    @NotNull(message = "Invitee id can not be null")
    @Schema(description = "Invitee's id", example = "663df880141d1c1227a7de70", required = true)
    private String inviteId;
    @NotNull(message = "Invitee email can not be null")
    @Schema(description = "Invitee's email", example = "john.doe@gmail.com", required = true)
    private String inviteeEmail;
    @NotNull(message = "Invitee password can not be null")
    @Schema(description = "Invitee's password", example = "encrypted@123", required = true)
    private String password;
}
