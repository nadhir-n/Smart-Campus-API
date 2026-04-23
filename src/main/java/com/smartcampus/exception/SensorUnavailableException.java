package com.smartcampus.exception;

public class SensorUnavailableException extends RuntimeException {
    public SensorUnavailableException() {
        super("Sensor is under maintenance and cannot accept readings");
    }
}