package com.salesflow.contact.dto;

import com.salesflow.contact.domain.TimelineEntry;
import lombok.Data;
import java.time.Instant;

@Data
public class TimelineEntryDTO {
    private Long id;
    private Long contactId;
    private String title;
    private String description;
    private TimelineEntry.EntryType type;
    private String createdBy;
    private Instant createdAt;
    private Instant updatedAt;
    private String updatedBy;
    private Long version;
} 