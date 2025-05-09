package com.salesflow.contact.core.service.impl;

import com.salesflow.contact.core.domain.Contact;
import com.salesflow.contact.core.dto.ContactDTO;
import com.salesflow.contact.core.exception.ContactNotFoundException;
import com.salesflow.contact.core.exception.DuplicateEmailException;
import com.salesflow.contact.core.mapper.ContactMapper;
import com.salesflow.contact.core.repository.ContactRepository;
import com.salesflow.contact.core.service.ContactService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ContactServiceImpl implements ContactService {

    private final ContactRepository contactRepository;
    private final ContactMapper contactMapper;

    @Override
    @Transactional
    public ContactDTO createContact(ContactDTO contactDTO, String ownerId) {
        if (contactRepository.existsByEmailAndIdNot(contactDTO.getEmail(), null)) {
            throw new DuplicateEmailException("Email already exists: " + contactDTO.getEmail());
        }

        Contact contact = contactMapper.toEntity(contactDTO);
        contact.setOwnerId(ownerId);
        contact = contactRepository.save(contact);
        return contactMapper.toDTO(contact);
    }

    @Override
    @Transactional
    public ContactDTO updateContact(Long id, ContactDTO contactDTO, String ownerId) {
        Contact contact = contactRepository.findById(id)
                .orElseThrow(() -> new ContactNotFoundException("Contact not found with id: " + id));

        if (!contact.getOwnerId().equals(ownerId)) {
            throw new ContactNotFoundException("Contact not found with id: " + id);
        }

        if (contactRepository.existsByEmailAndIdNot(contactDTO.getEmail(), id)) {
            throw new DuplicateEmailException("Email already exists: " + contactDTO.getEmail());
        }

        contactMapper.updateEntityFromDTO(contactDTO, contact);
        contact = contactRepository.save(contact);
        return contactMapper.toDTO(contact);
    }

    @Override
    @Transactional
    public void deleteContact(Long id, String ownerId) {
        Contact contact = contactRepository.findById(id)
                .orElseThrow(() -> new ContactNotFoundException("Contact not found with id: " + id));

        if (!contact.getOwnerId().equals(ownerId)) {
            throw new ContactNotFoundException("Contact not found with id: " + id);
        }

        contactRepository.delete(contact);
    }

    @Override
    @Transactional(readOnly = true)
    public ContactDTO getContact(Long id, String ownerId) {
        Contact contact = contactRepository.findById(id)
                .orElseThrow(() -> new ContactNotFoundException("Contact not found with id: " + id));

        if (!contact.getOwnerId().equals(ownerId)) {
            throw new ContactNotFoundException("Contact not found with id: " + id);
        }

        return contactMapper.toDTO(contact);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ContactDTO> getContacts(String ownerId, Pageable pageable) {
        return contactRepository.findByOwnerId(ownerId, pageable)
                .map(contactMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ContactDTO> searchContacts(String ownerId, String searchTerm, Pageable pageable) {
        return contactRepository.searchContacts(ownerId, searchTerm, pageable)
                .map(contactMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContactDTO> getContactsByStage(String ownerId, Contact.ContactStage stage) {
        return contactRepository.findByOwnerIdAndStage(ownerId, stage)
                .stream()
                .map(contactMapper::toDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public long countContactsByStage(String ownerId, Contact.ContactStage stage) {
        return contactRepository.countByOwnerIdAndStage(ownerId, stage);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return contactRepository.findByEmail(email).isPresent();
    }
} 