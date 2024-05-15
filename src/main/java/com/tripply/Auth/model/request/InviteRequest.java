package com.tripply.Auth.model.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class InviteRequest {

    @NotNull(message = "Invite Id can not be null")
    private String inviteId;
    private String inviteeEmail;
    private String password;
}
