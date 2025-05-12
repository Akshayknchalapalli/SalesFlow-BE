package com.salesflow.activity.dto;

import com.salesflow.activity.model.ActivityType;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActivityDTO {
    private Long id;
    
    @NotNull(message = "Contact ID is required")
    private Long contactId;
    
    @NotNull(message = "Activity type is required")
    private ActivityType type;
    
    @NotNull(message = "Outcome is required")
    private String outcome;
    
    private String notes;
    
    private LocalDateTime scheduledAt;
    
    private LocalDateTime completedAt;
    
    private String assignedTo;
    
    private String nextStep;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
} 