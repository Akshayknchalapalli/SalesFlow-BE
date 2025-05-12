package com.salesflow.contact.dto;

import com.salesflow.contact.domain.Contact;
import lombok.Data;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Data
public class ContactDTO {
    private UUID id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String companyName;
    private String jobTitle;
    private Contact.ContactStage stage;
    private String ownerId;
    private ContactPreferencesDTO preferences;
    private Set<AddressDTO> addresses;
    private Set<SocialProfileDTO> socialProfiles;
    private Set<NoteDTO> notes;
    private Set<TagDTO> tags;
    private Set<TimelineEntryDTO> timelineEntries;
    private Set<ContactDTO> relatedContacts;
    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;
    private Long version;
} 