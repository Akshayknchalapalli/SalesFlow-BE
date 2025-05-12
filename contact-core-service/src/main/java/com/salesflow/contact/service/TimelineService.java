package com.salesflow.contact.service;

import com.salesflow.contact.domain.TimelineEntry;
import com.salesflow.contact.dto.TimelineEntryDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface TimelineService {
    
    TimelineEntryDTO createTimelineEntry(TimelineEntryDTO entryDTO, String userId);
    
    Page<TimelineEntryDTO> getContactTimeline(UUID contactId, Pageable pageable);
    
    List<TimelineEntryDTO> getContactTimelineByType(UUID contactId, TimelineEntry.EntryType type);
    
    long countContactTimelineEntriesByType(UUID contactId, TimelineEntry.EntryType type);
    
    void createContactCreatedEntry(UUID contactId, String userId);
    
    void createStageChangedEntry(UUID contactId, String oldStage, String newStage, String userId);
    
    void createActivityLoggedEntry(UUID contactId, String activityType, String description, String userId);
    
    void createNoteAddedEntry(UUID contactId, String note, String userId);
    
    void createTagAddedEntry(UUID contactId, String tagName, String userId);
    
    void createTagRemovedEntry(UUID contactId, String tagName, String userId);
    
    void createOwnershipChangedEntry(UUID contactId, String oldOwner, String newOwner, String userId);
} 