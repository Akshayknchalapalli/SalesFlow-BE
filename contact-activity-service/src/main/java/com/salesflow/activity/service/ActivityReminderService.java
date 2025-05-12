package com.salesflow.activity.service;

import com.salesflow.activity.model.Activity;
import com.salesflow.activity.repository.ActivityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ActivityReminderService {
    private final ActivityRepository activityRepository;
    private final ActivityNotificationService notificationService;

    @Scheduled(cron = "0 0 9 * * ?") // Run at 9 AM every day
    public void checkUpcomingActivities() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime tomorrow = now.plusDays(1);
        
        List<Activity> upcomingActivities = activityRepository.findByScheduledAtBetween(now, tomorrow);
        
        for (Activity activity : upcomingActivities) {
            if (activity.getCompletedAt() == null) {
                notificationService.sendReminder(activity);
            }
        }
    }

    @Scheduled(cron = "0 0 17 * * ?") // Run at 5 PM every day
    public void checkOverdueActivities() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime yesterday = now.minusDays(1);
        
        List<Activity> overdueActivities = activityRepository.findByScheduledAtBetween(yesterday, now);
        
        for (Activity activity : overdueActivities) {
            if (activity.getCompletedAt() == null) {
                notificationService.sendOverdueNotification(activity);
            }
        }
    }
} 