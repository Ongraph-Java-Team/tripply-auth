package com.tripply.Auth.constants;

import com.tripply.Auth.exception.RecordNotFoundException;

public enum Status {
    PENDING("Pending"),
    ACCEPTED("Accepted"),
    REJECTED("Rejected");

    private String value;

    Status(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static Status fromValue(String value) {
        for (Status status : Status.values()) {
            if (status.value.equalsIgnoreCase(value)) {
                return status;
            }
        }
        throw new RecordNotFoundException("Unknown enum value: " + value);
    }
}