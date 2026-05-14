package com.sujan.studysync.exception;

public class NotMemberException extends RuntimeException {
    public NotMemberException() {
        super("You are not a member of this room");
    }
}
