package com.salesflow.activity.dto;

import com.salesflow.activity.model.ActivityType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
public class ActivityDTO {
    private UUID id;
    
    @NotNull(message = "Contact ID is required")
    private UUID contactId;
    
    @NotNull(message = "Activity type is required")
    private ActivityType type;
    
    @NotNull(message = "Title is required")
    private String title;
    
    private String description;
    private String status;
    private String priority;
    private LocalDateTime scheduledTime;
    private LocalDateTime completedTime;
    private String assignedTo;
    private String createdBy;
    private String updatedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
} 