package com.smartcampus.exception;

public class LinkedResourceNotFoundException extends RuntimeException {
    private final String missingId;

    public LinkedResourceNotFoundException(String missingId) {
        super("Referenced resource not found: " + missingId);
        this.missingId = missingId;
    }

    public String getMissingId() { return missingId; }
}