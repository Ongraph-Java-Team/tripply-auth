package com.tripply.Auth.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AuthConstants {

    public static final String GET_NOTIFICATION_URL = "/notification/invite";
    public static final String ONBOARD_HOTEL = "/booking/onboard-hotel";
    public static final String BEARER = "Bearer ";
    public static final String DUMMY_TOKEN = "dummy-token";
    public static final String SEND_FORGET_PASSWORD_EMAIL = "/notification/forgot-password";
}
