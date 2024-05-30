package com.tripply.Auth.model.response;

import com.tripply.Auth.constants.Status;
import com.tripply.Auth.dto.HotelDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InvitationDetailResponse {
    private String invitationId;
    private String sentToEmail;
    private String category;
    private Status status;
    private String sendToName;
    private HotelDto hotelRequest;
}
