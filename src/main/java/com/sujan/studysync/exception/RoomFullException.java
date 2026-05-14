package com.sujan.studysync.exception;

public class RoomFullException extends RuntimeException {
    public RoomFullException() {
        super("This room has reached its maximum member limit");
    }
}
