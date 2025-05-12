package com.salesflow.activity.service;

import com.salesflow.activity.model.Activity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ActivityNotificationService {

    public void sendReminder(Activity activity) {
        // TODO: Integrate with your notification system (email, SMS, etc.)
        log.info("Sending reminder for activity: {} to {}", activity.getId(), activity.getAssignedTo());
        // Example: emailService.sendEmail(activity.getAssignedTo(), "Activity Reminder", buildReminderMessage(activity));
    }

    public void sendOverdueNotification(Activity activity) {
        // TODO: Integrate with your notification system
        log.info("Sending overdue notification for activity: {} to {}", activity.getId(), activity.getAssignedTo());
        // Example: emailService.sendEmail(activity.getAssignedTo(), "Overdue Activity", buildOverdueMessage(activity));
    }

    private String buildReminderMessage(Activity activity) {
        return String.format(
            "Reminder: You have an upcoming activity scheduled for %s.\n" +
            "Type: %s\n" +
            "Title: %s\n" +
            "Description: %s",
            activity.getScheduledTime(),
            activity.getType(),
            activity.getTitle(),
            activity.getDescription()
        );
    }

    private String buildOverdueMessage(Activity activity) {
        return String.format(
            "URGENT: You have an overdue activity that was scheduled for %s.\n" +
            "Type: %s\n" +
            "Title: %s\n" +
            "Description: %s",
            activity.getScheduledTime(),
            activity.getType(),
            activity.getTitle(),
            activity.getDescription()
        );
    }
} 