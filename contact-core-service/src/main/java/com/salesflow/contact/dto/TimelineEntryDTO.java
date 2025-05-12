package com.salesflow.contact.dto;

import com.salesflow.contact.domain.TimelineEntry;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimelineEntryDTO {
    private UUID id;
    private UUID contactId;
    private String title;
    private String description;
    private TimelineEntry.EntryType type;
    private String createdBy;
    private Instant createdAt;
    private Instant updatedAt;
    private String updatedBy;
    private Long version;
} 