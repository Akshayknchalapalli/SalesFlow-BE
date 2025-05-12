package com.salesflow.activity.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TimelineDTO {
    private UUID contactId;
    private List<ActivityDTO> activities;
} 