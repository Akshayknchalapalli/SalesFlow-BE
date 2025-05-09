package com.salesflow.contact.core.service;

import com.salesflow.contact.core.domain.Contact;
import com.salesflow.contact.core.dto.ContactDTO;
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
} 