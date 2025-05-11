package com.salesflow.contact.service;

import com.salesflow.contact.domain.Contact;
import com.salesflow.contact.dto.ContactDTO;
import com.salesflow.contact.dto.TagDTO;
import com.salesflow.contact.dto.NoteDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ContactService {
    
    ContactDTO createContact(ContactDTO contactDTO, String ownerId);
    
    ContactDTO updateContact(Long id, ContactDTO contactDTO, String ownerId);
    
    void deleteContact(Long id, String ownerId);
    
    ContactDTO getContact(Long id, String ownerId);
    
    Page<ContactDTO> getContacts(String ownerId, Pageable pageable);
    
    Page<ContactDTO> searchContacts(String ownerId, String searchTerm, Pageable pageable);
    
    List<ContactDTO> getContactsByStage(String ownerId, Contact.ContactStage stage);
    
    long countContactsByStage(String ownerId, Contact.ContactStage stage);
    
    boolean existsByEmail(String email);
    
    List<ContactDTO> createBulkContacts(List<ContactDTO> contactDTOs, String ownerId);

    void addTagToContact(Long contactId, Long tagId, String ownerId);

    void removeTagFromContact(Long contactId, Long tagId, String ownerId);

    List<TagDTO> getContactTags(Long contactId, String ownerId);

    void addNoteToContact(Long contactId, String note, String ownerId); 

    void removeNoteFromContact(Long contactId, Long noteId, String ownerId);

    List<NoteDTO> getContactNotes(Long contactId, String ownerId);
} 