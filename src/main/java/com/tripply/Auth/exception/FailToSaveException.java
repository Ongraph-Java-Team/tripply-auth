package com.tripply.Auth.exception;

public class FailToSaveException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public FailToSaveException() {
        super();
    }

    public FailToSaveException(String message) {
        super(message);
    }

    public FailToSaveException(String message, Throwable cause) {
        super(message, cause);
    }
}
