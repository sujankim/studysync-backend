package com.sujan.studysync.exception;


public class RoomNotFoundException extends RuntimeException {

    public RoomNotFoundException(Long id) {
        super("Study room not found with id: " + id);
    }

    public RoomNotFoundException(String message) {
        super(message);
    }
}
