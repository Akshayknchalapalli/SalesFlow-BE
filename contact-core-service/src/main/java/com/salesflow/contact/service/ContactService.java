package com.salesflow.contact.service;

import com.salesflow.contact.domain.Contact;
import com.salesflow.contact.dto.ContactDTO;
import com.salesflow.contact.dto.TagDTO;
import com.salesflow.contact.dto.NoteDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface ContactService {
    
    ContactDTO createContact(ContactDTO contactDTO, String ownerId);
    
    ContactDTO updateContact(UUID id, ContactDTO contactDTO, String ownerId);
    
    void deleteContact(UUID id, String ownerId);
    
    ContactDTO getContact(UUID id, String ownerId);
    
    Page<ContactDTO> getContacts(String ownerId, Pageable pageable);
    
    Page<ContactDTO> searchContacts(String ownerId, String searchTerm, Pageable pageable);
    
    List<ContactDTO> getContactsByStage(String ownerId, Contact.ContactStage stage);
    
    long countContactsByStage(String ownerId, Contact.ContactStage stage);
    
    boolean existsByEmail(String email);
    
    boolean existsByEmailAndIdNot(String email, UUID id);
    
    List<ContactDTO> createBulkContacts(List<ContactDTO> contactDTOs, String ownerId);

    void addTagToContact(UUID contactId, UUID tagId, String ownerId);

    void removeTagFromContact(UUID contactId, UUID tagId, String ownerId);

    List<TagDTO> getContactTags(UUID contactId, String ownerId);

    void addNoteToContact(UUID contactId, String note, String ownerId); 

    void removeNoteFromContact(UUID contactId, UUID noteId, String ownerId);

    List<NoteDTO> getContactNotes(UUID contactId, String ownerId);
} 