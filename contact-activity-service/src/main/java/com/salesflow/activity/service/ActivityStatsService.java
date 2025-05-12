package com.salesflow.activity.service;

import com.salesflow.activity.dto.ActivityStatsDTO;
import com.salesflow.activity.model.Activity;
import com.salesflow.activity.model.ActivityType;
import com.salesflow.activity.repository.ActivityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ActivityStatsService {
    private final ActivityRepository activityRepository;

    public ActivityStatsDTO getContactActivityStats(Long contactId) {
        List<Activity> activities = activityRepository.findByContactId(contactId);
        
        int totalActivities = activities.size();
        int completedActivities = (int) activities.stream()
                .filter(a -> a.getCompletedAt() != null)
                .count();
        int pendingActivities = totalActivities - completedActivities;
        
        Map<ActivityType, Integer> activitiesByType = activities.stream()
                .collect(Collectors.groupingBy(
                        Activity::getType,
                        Collectors.collectingAndThen(Collectors.counting(), Long::intValue)
                ));
        
        double completionRate = totalActivities > 0 
                ? (double) completedActivities / totalActivities * 100 
                : 0;
        
        int averageResponseTime = calculateAverageResponseTime(activities);
        
        return new ActivityStatsDTO(
                contactId,
                totalActivities,
                completedActivities,
                pendingActivities,
                activitiesByType,
                completionRate,
                averageResponseTime
        );
    }

    private int calculateAverageResponseTime(List<Activity> activities) {
        return (int) activities.stream()
                .filter(a -> a.getCompletedAt() != null && a.getScheduledAt() != null)
                .mapToLong(a -> Duration.between(a.getScheduledAt(), a.getCompletedAt()).toHours())
                .average()
                .orElse(0);
    }
} 