package com.salesflow.activity.service;

import com.salesflow.activity.model.Activity;
import com.salesflow.activity.repository.ActivityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ActivityReminderService {
    private final ActivityRepository activityRepository;

    @Scheduled(cron = "0 0 9 * * ?") // Run at 9 AM every day
    @Transactional(readOnly = true)
    public void sendDailyReminders() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endOfDay = now.plusDays(1).withHour(0).withMinute(0).withSecond(0);
        
        List<Activity> upcomingActivities = activityRepository.findByScheduledTimeBetween(now, endOfDay);
        
        for (Activity activity : upcomingActivities) {
            log.info("Sending reminder for activity: {} scheduled at {}", 
                    activity.getTitle(), activity.getScheduledTime());
            // TODO: Implement actual reminder sending logic (email, notification, etc.)
        }
    }

    @Scheduled(cron = "0 0 * * * ?") // Run every hour
    @Transactional(readOnly = true)
    public void sendHourlyReminders() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneHourFromNow = now.plusHours(1);
        
        List<Activity> upcomingActivities = activityRepository.findByScheduledTimeBetween(now, oneHourFromNow);
        
        for (Activity activity : upcomingActivities) {
            log.info("Sending urgent reminder for activity: {} scheduled at {}", 
                    activity.getTitle(), activity.getScheduledTime());
            // TODO: Implement actual reminder sending logic (email, notification, etc.)
        }
    }
} 