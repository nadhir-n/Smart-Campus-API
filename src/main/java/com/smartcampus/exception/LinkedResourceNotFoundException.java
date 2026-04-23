package com.smartcampus.exception;

public class LinkedResourceNotFoundException extends RuntimeException {
    public LinkedResourceNotFoundException() {
        super("Referenced resource does not exist");
    }
}