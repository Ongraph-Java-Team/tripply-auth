package com.tripply.Auth.constants;

public enum ErrorConstant {

    ER001("0", "ER001", "Record not found in tripply system"),
    ER002("1", "ER002", "We're sorry, but the service you're trying to access is temporarily unavailable. Please try again later."),
    ER003("2", "ER003", "We apologize for the inconvenience, but due to an internal error, our service is currently unable to process your request."),
    ER004("3", "ER004", "Invalid email or password entered"),
    ER005("4", "ER005", "The request could not be understood or was missing required parameters."),
    ER006("5", "ER006", "The requested endpoint does not exist."),
    ER007("6", "ER007", "The account has not been verified yet"),
    ER008("7", "ER008", "You do not have permission to access this resource."),
    ER009("8", "ER009", "Your session has expired. Please log in again to continue.");

    private final String key;
    private final String errorCode;
    private final String errorDescription;

    ErrorConstant(String key, String errorCode, String errorDescription) {
        this.key = key;
        this.errorCode = errorCode;
        this.errorDescription = errorDescription;
    }

    public String getKey() {
        return key;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getErrorDescription() {
        return errorDescription;
    }

    public static ErrorConstant getByErrorCode(String errorCode) {
        for (ErrorConstant constant : ErrorConstant.values()) {
            if (constant.errorCode.equals(errorCode)) {
                return constant;
            }
        }
        return null;
    }

    public static ErrorConstant getByKey(String key) {
        for (ErrorConstant constant : ErrorConstant.values()) {
            if (constant.key.equals(key)) {
                return constant;
            }
        }
        return null;
    }
}
