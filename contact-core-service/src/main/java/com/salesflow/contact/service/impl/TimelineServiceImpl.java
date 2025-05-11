package com.salesflow.contact.service.impl;

import com.salesflow.contact.domain.Contact;
import com.salesflow.contact.domain.TimelineEntry;
import com.salesflow.contact.dto.TimelineEntryDTO;
import com.salesflow.contact.exception.ContactNotFoundException;
import com.salesflow.contact.mapper.TimelineEntryMapper;
import com.salesflow.contact.repository.ContactRepository;
import com.salesflow.contact.repository.TimelineEntryRepository;
import com.salesflow.contact.service.TimelineService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TimelineServiceImpl implements TimelineService {

    private final TimelineEntryRepository timelineEntryRepository;
    private final ContactRepository contactRepository;
    private final TimelineEntryMapper timelineEntryMapper;

    @Override
    @Transactional
    public TimelineEntryDTO createTimelineEntry(TimelineEntryDTO entryDTO, String userId) {
        Contact contact = contactRepository.findById(entryDTO.getContactId())
                .orElseThrow(() -> new ContactNotFoundException("Contact not found with id: " + entryDTO.getContactId()));

        TimelineEntry entry = timelineEntryMapper.toEntity(entryDTO);
        entry.setContact(contact);
        entry.setCreatedBy(userId);
        entry.setUpdatedBy(userId);
        entry = timelineEntryRepository.save(entry);
        return timelineEntryMapper.toDTO(entry);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TimelineEntryDTO> getContactTimeline(Long contactId, Pageable pageable) {
        return timelineEntryRepository.findByContactId(contactId, pageable)
                .map(timelineEntryMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TimelineEntryDTO> getContactTimelineByType(Long contactId, TimelineEntry.EntryType type) {
        return timelineEntryRepository.findByContactIdAndTypeOrderByCreatedAtDesc(contactId, type)
                .stream()
                .map(timelineEntryMapper::toDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public long countContactTimelineEntriesByType(Long contactId, TimelineEntry.EntryType type) {
        return timelineEntryRepository.countByContactIdAndType(contactId, type);
    }

    @Override
    @Transactional
    public void createContactCreatedEntry(Long contactId, String userId) {
        Contact contact = contactRepository.findById(contactId)
                .orElseThrow(() -> new ContactNotFoundException("Contact not found with id: " + contactId));

        TimelineEntry entry = TimelineEntry.builder()
                .contact(contact)
                .title("Contact Created")
                .description("New contact created: " + contact.getFirstName() + " " + contact.getLastName())
                .type(TimelineEntry.EntryType.CONTACT_CREATED)
                .createdBy(userId)
                .updatedBy(userId)
                .build();

        timelineEntryRepository.save(entry);
    }

    @Override
    @Transactional
    public void createStageChangedEntry(Long contactId, String oldStage, String newStage, String userId) {
        Contact contact = contactRepository.findById(contactId)
                .orElseThrow(() -> new ContactNotFoundException("Contact not found with id: " + contactId));

        TimelineEntry entry = TimelineEntry.builder()
                .contact(contact)
                .title("Stage Changed")
                .description("Stage changed from " + oldStage + " to " + newStage)
                .type(TimelineEntry.EntryType.STAGE_CHANGED)
                .createdBy(userId)
                .updatedBy(userId)
                .build();

        timelineEntryRepository.save(entry);
    }

    @Override
    @Transactional
    public void createActivityLoggedEntry(Long contactId, String activityType, String description, String userId) {
        Contact contact = contactRepository.findById(contactId)
                .orElseThrow(() -> new ContactNotFoundException("Contact not found with id: " + contactId));

        TimelineEntry entry = TimelineEntry.builder()
                .contact(contact)
                .title("Activity Logged: " + activityType)
                .description(description)
                .type(TimelineEntry.EntryType.ACTIVITY_LOGGED)
                .createdBy(userId)
                .updatedBy(userId)
                .build();

        timelineEntryRepository.save(entry);
    }

    @Override
    @Transactional
    public void createNoteAddedEntry(Long contactId, String note, String userId) {
        Contact contact = contactRepository.findById(contactId)
                .orElseThrow(() -> new ContactNotFoundException("Contact not found with id: " + contactId));

        TimelineEntry entry = TimelineEntry.builder()
                .contact(contact)
                .title("Note Added")
                .description(note)
                .type(TimelineEntry.EntryType.NOTE_ADDED)
                .createdBy(userId)
                .updatedBy(userId)
                .build();

        timelineEntryRepository.save(entry);
    }

    @Override
    @Transactional
    public void createTagAddedEntry(Long contactId, String tagName, String userId) {
        Contact contact = contactRepository.findById(contactId)
                .orElseThrow(() -> new ContactNotFoundException("Contact not found with id: " + contactId));

        TimelineEntry entry = TimelineEntry.builder()
                .contact(contact)
                .title("Tag Added")
                .description("Tag '" + tagName + "' added to contact")
                .type(TimelineEntry.EntryType.TAG_ADDED)
                .createdBy(userId)
                .updatedBy(userId)
                .build();

        timelineEntryRepository.save(entry);
    }

    @Override
    @Transactional
    public void createTagRemovedEntry(Long contactId, String tagName, String userId) {
        Contact contact = contactRepository.findById(contactId)
                .orElseThrow(() -> new ContactNotFoundException("Contact not found with id: " + contactId));

        TimelineEntry entry = TimelineEntry.builder()
                .contact(contact)
                .title("Tag Removed")
                .description("Tag '" + tagName + "' removed from contact")
                .type(TimelineEntry.EntryType.TAG_REMOVED)
                .createdBy(userId)
                .updatedBy(userId)
                .build();

        timelineEntryRepository.save(entry);
    }

    @Override
    @Transactional
    public void createOwnershipChangedEntry(Long contactId, String oldOwner, String newOwner, String userId) {
        Contact contact = contactRepository.findById(contactId)
                .orElseThrow(() -> new ContactNotFoundException("Contact not found with id: " + contactId));

        TimelineEntry entry = TimelineEntry.builder()
                .contact(contact)
                .title("Ownership Changed")
                .description("Ownership changed from " + oldOwner + " to " + newOwner)
                .type(TimelineEntry.EntryType.OWNERSHIP_CHANGED)
                .createdBy(userId)
                .updatedBy(userId)
                .build();

        timelineEntryRepository.save(entry);
    }
} 