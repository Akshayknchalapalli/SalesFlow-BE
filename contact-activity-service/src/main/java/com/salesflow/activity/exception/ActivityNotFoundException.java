package com.salesflow.activity.exception;

import java.util.UUID;

public class ActivityNotFoundException extends RuntimeException {
    public ActivityNotFoundException(String message) {
        super(message);
    }

    public ActivityNotFoundException(UUID activityId) {
        super("Activity not found with ID: " + activityId);
    }
} 