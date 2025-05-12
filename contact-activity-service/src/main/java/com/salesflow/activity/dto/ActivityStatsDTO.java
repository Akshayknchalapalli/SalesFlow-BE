package com.salesflow.activity.dto;

import com.salesflow.activity.model.ActivityType;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActivityStatsDTO {
    private Long contactId;
    private int totalActivities;
    private int completedActivities;
    private int pendingActivities;
    private Map<ActivityType, Integer> activitiesByType;
    private double completionRate;
    private int averageResponseTime; // in hours
} 