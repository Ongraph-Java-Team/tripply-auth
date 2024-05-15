package com.tripply.Auth.dto;

import lombok.Data;

import java.util.List;

@Data
public class HotelDto {

    private String name;
    private String registrationNumber;
    private String address;
    private String city;
    private String stateId;
    private String countryId;
    private String description;
    private String website;
    private List<Amenity> amenities;
    private ManagerDetails managerDetails;
}
