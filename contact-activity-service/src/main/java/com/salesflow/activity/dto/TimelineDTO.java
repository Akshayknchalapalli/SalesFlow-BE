package com.salesflow.activity.dto;

import com.salesflow.activity.model.ActivityType;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TimelineDTO {
    private Long id;
    private Long contactId;
    private ActivityType type;
    private String outcome;
    private String notes;
    private LocalDateTime timestamp;
    private String assignedTo;
    private String nextStep;
    private boolean isCompleted;
    private LocalDateTime completedAt;
} 