package com.salesflow.activity.exception;

public class InvalidActivityStateException extends RuntimeException {
    public InvalidActivityStateException(String message) {
        super(message);
    }

    public InvalidActivityStateException(String currentState, String attemptedAction) {
        super("Cannot " + attemptedAction + " activity in " + currentState + " state");
    }
} 