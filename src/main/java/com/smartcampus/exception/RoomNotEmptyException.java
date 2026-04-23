package com.smartcampus.exception;

public class RoomNotEmptyException extends RuntimeException {
    public RoomNotEmptyException() {
        super("Room still has sensors assigned");
    }
}