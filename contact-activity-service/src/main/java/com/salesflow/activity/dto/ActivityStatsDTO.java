package com.salesflow.activity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActivityStatsDTO {
    private UUID contactId;
    private int totalActivities;
    private int completedActivities;
} 