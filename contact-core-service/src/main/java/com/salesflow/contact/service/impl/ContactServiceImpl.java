package com.salesflow.contact.service.impl;

import com.salesflow.contact.domain.Contact;
import com.salesflow.contact.domain.Note;
import com.salesflow.contact.domain.Tag;
import com.salesflow.contact.dto.ContactDTO;
import com.salesflow.contact.dto.NoteDTO;
import com.salesflow.contact.dto.TagDTO;
import com.salesflow.contact.exception.ContactNotFoundException;
import com.salesflow.contact.exception.DuplicateEmailException;
import com.salesflow.contact.exception.NoteNotFoundException;
import com.salesflow.contact.exception.TagNotFoundException;
import com.salesflow.contact.mapper.ContactMapper;
import com.salesflow.contact.mapper.NoteMapper;
import com.salesflow.contact.mapper.TagMapper;
import com.salesflow.contact.repository.ContactRepository;
import com.salesflow.contact.repository.TagRepository;
import com.salesflow.contact.service.ContactService;
import com.salesflow.contact.service.NoteService;
import com.salesflow.contact.service.TimelineService;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ContactServiceImpl implements ContactService {

    private final ContactRepository contactRepository;
    private final ContactMapper contactMapper;
    private final TimelineService timelineService;
    private final TagRepository tagRepository;
    private final TagMapper tagMapper;
    private final NoteService noteService;

    @Override
    @Transactional
    public ContactDTO createContact(ContactDTO contactDTO, String ownerId) {
        if (contactRepository.existsByEmailAndIdNot(contactDTO.getEmail(), null)) {
            throw new DuplicateEmailException("Email already exists: " + contactDTO.getEmail());
        }

        Contact contact = contactMapper.toEntity(contactDTO);
        contact.setOwnerId(ownerId);
        contact.setCreatedBy(ownerId);
        contact.setUpdatedBy(ownerId);

        // Set audit fields for addresses
        if (contact.getAddresses() != null) {
            contact.getAddresses().forEach(address -> {
                address.setCreatedBy(ownerId);
                address.setUpdatedBy(ownerId);
                address.setCreatedAt(Instant.now());
                address.setUpdatedAt(Instant.now());
            });
        }

        // Set audit fields for social profiles
        if (contact.getSocialProfiles() != null) {
            contact.getSocialProfiles().forEach(profile -> {
                profile.setCreatedBy(ownerId);
                profile.setUpdatedBy(ownerId);
                profile.setCreatedAt(Instant.now());
                profile.setUpdatedAt(Instant.now());
                profile.setVersion(0L);
            });
        }

        contact = contactRepository.save(contact);

        // Create a timeline entry for the contact
        timelineService.createContactCreatedEntry(contact.getId(), ownerId);

        return contactMapper.toDTO(contact);
    }

    @Override
    @Transactional
    public ContactDTO updateContact(UUID id, ContactDTO contactDTO, String ownerId) {
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

        timelineService.createStageChangedEntry(contact.getId(), contact.getStage().name(), contactDTO.getStage().name(), ownerId);
        return contactMapper.toDTO(contact);
    }

    @Override
    @Transactional
    public void deleteContact(UUID id, String ownerId) {
        Contact contact = contactRepository.findById(id)
                .orElseThrow(() -> new ContactNotFoundException("Contact not found with id: " + id));

        if (!contact.getOwnerId().equals(ownerId)) {
            throw new ContactNotFoundException("Contact not found with id: " + id);
        }

        contactRepository.delete(contact);
        timelineService.createActivityLoggedEntry(contact.getId(), "Delete", "Deleted contact", ownerId);
    }

    @Override
    @Transactional(readOnly = true)
    public ContactDTO getContact(UUID id, String ownerId) {
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

    @Override
    @Transactional
    public List<ContactDTO> createBulkContacts(List<ContactDTO> contactDTOs, String ownerId) {
        List<Contact> contacts = contactDTOs.stream()
            .map(contactDTO -> {
                Contact contact = contactMapper.toEntity(contactDTO);
                contact.setOwnerId(ownerId);
                contact.setCreatedBy(ownerId);
                contact.setUpdatedBy(ownerId);
                return contact;
            })
            .toList();

        List<Contact> savedContacts = contactRepository.saveAll(contacts);
        timelineService.createActivityLoggedEntry(null, "Bulk Create", "Created multiple contacts", ownerId);
        return savedContacts.stream()
            .map(contactMapper::toDTO)
            .toList();
    }

    @Override
    @Transactional
    public void addTagToContact(UUID contactId, UUID tagId, String ownerId) {
        Contact contact = contactRepository.findById(contactId)
                .orElseThrow(() -> new ContactNotFoundException("Contact not found with id: " + contactId));

        if (!contact.getOwnerId().equals(ownerId)) {
            throw new ContactNotFoundException("Contact not found with id: " + contactId);
        }

        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new TagNotFoundException("Tag not found with id: " + tagId));

        contact.addTag(tag);
        contactRepository.save(contact);
    }

    @Override
    @Transactional
    public void removeTagFromContact(UUID contactId, UUID tagId, String ownerId) {
        Contact contact = contactRepository.findById(contactId)
                .orElseThrow(() -> new ContactNotFoundException("Contact not found with id: " + contactId));

        if (!contact.getOwnerId().equals(ownerId)) {
            throw new ContactNotFoundException("Contact not found with id: " + contactId);
        }

        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new TagNotFoundException("Tag not found with id: " + tagId));

        contact.removeTag(tag);
        contactRepository.save(contact);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TagDTO> getContactTags(UUID contactId, String ownerId) {
        Contact contact = contactRepository.findById(contactId)
                .orElseThrow(() -> new ContactNotFoundException("Contact not found with id: " + contactId));

        if (!contact.getOwnerId().equals(ownerId)) {
            throw new ContactNotFoundException("Contact not found with id: " + contactId);
        }

        return contact.getTags().stream()
                .map(tagMapper::toDTO)
                .toList();
    }

    @Override
    @Transactional
    public void addNoteToContact(UUID contactId, String noteContent, String ownerId) {
        Contact contact = contactRepository.findById(contactId)
                .orElseThrow(() -> new ContactNotFoundException("Contact not found with id: " + contactId));

        if (!contact.getOwnerId().equals(ownerId)) {
            throw new ContactNotFoundException("Contact not found with id: " + contactId);
        }

        NoteDTO noteDTO = NoteDTO.builder()
                .content(noteContent)
                .contactId(contactId)
                .build();

        noteService.createNote(noteDTO, ownerId);
    }

    @Override
    @Transactional
    public void removeNoteFromContact(UUID contactId, UUID noteId, String ownerId) {
        Contact contact = contactRepository.findById(contactId)
                .orElseThrow(() -> new ContactNotFoundException("Contact not found with id: " + contactId));

        if (!contact.getOwnerId().equals(ownerId)) {
            throw new ContactNotFoundException("Contact not found with id: " + contactId);
        }

        noteService.deleteNote(noteId, ownerId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NoteDTO> getContactNotes(UUID contactId, String ownerId) {
        Contact contact = contactRepository.findById(contactId)
                .orElseThrow(() -> new ContactNotFoundException("Contact not found with id: " + contactId));

        if (!contact.getOwnerId().equals(ownerId)) {
            throw new ContactNotFoundException("Contact not found with id: " + contactId);
        }

        return noteService.getContactNotes(contactId, ownerId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmailAndIdNot(String email, UUID id) {
        return contactRepository.existsByEmailAndIdNot(email, id);
    }
} 