package com.salesflow.activity.service;

import com.salesflow.activity.dto.ActivityStatsDTO;
import com.salesflow.activity.model.Activity;
import com.salesflow.activity.model.ActivityType;
import com.salesflow.activity.repository.ActivityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ActivityStatsService {
    private final ActivityRepository activityRepository;

    @Transactional(readOnly = true)
    public ActivityStatsDTO getActivityStats(UUID contactId) {
        List<Activity> activities = activityRepository.findByContactId(contactId);
        
        int totalActivities = activities.size();
        int completedActivities = (int) activities.stream()
                .filter(a -> a.getCompletedTime() != null)
                .count();
        
        ActivityStatsDTO stats = new ActivityStatsDTO();
        stats.setContactId(contactId);
        stats.setTotalActivities(totalActivities);
        stats.setCompletedActivities(completedActivities);
        
        return stats;
    }

    private int calculateAverageResponseTime(List<Activity> activities) {
        return (int) activities.stream()
                .filter(a -> a.getCompletedTime() != null && a.getScheduledTime() != null)
                .mapToLong(a -> Duration.between(a.getScheduledTime(), a.getCompletedTime()).toHours())
                .average()
                .orElse(0);
    }
} 