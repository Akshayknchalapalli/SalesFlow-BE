package com.salesflow.contact.service;

import com.salesflow.contact.domain.TimelineEntry;
import com.salesflow.contact.dto.TimelineEntryDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface TimelineService {
    
    TimelineEntryDTO createTimelineEntry(TimelineEntryDTO entryDTO, String userId);
    
    Page<TimelineEntryDTO> getContactTimeline(Long contactId, Pageable pageable);
    
    List<TimelineEntryDTO> getContactTimelineByType(Long contactId, TimelineEntry.EntryType type);
    
    long countContactTimelineEntriesByType(Long contactId, TimelineEntry.EntryType type);
    
    void createContactCreatedEntry(Long contactId, String userId);
    
    void createStageChangedEntry(Long contactId, String oldStage, String newStage, String userId);
    
    void createActivityLoggedEntry(Long contactId, String activityType, String description, String userId);
    
    void createNoteAddedEntry(Long contactId, String note, String userId);
    
    void createTagAddedEntry(Long contactId, String tagName, String userId);
    
    void createTagRemovedEntry(Long contactId, String tagName, String userId);
    
    void createOwnershipChangedEntry(Long contactId, String oldOwner, String newOwner, String userId);
} 