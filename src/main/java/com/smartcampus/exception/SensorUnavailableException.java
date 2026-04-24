package com.smartcampus.exception;

public class SensorUnavailableException extends RuntimeException {
    private final String sensorId;

    public SensorUnavailableException(String sensorId, String status) {
        super("Sensor " + sensorId + " is currently in status '" + status + "' and cannot accept readings.");
        this.sensorId = sensorId;
    }

    public String getSensorId() { return sensorId; }
}