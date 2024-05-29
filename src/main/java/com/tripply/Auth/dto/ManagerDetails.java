package com.tripply.Auth.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class ManagerDetails {

    private UUID userId;
    private String email;
    private String phoneNumber;
    private String countryCode;
    private String firstName;
    private String lastName;
}
