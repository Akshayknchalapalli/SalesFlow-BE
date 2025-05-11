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
import com.salesflow.contact.service.TimelineService;

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
    private final TimelineService timelineService;
    private final TagRepository tagRepository;
    private final TagMapper tagMapper;
    private final NoteMapper noteMapper;

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
        contact = contactRepository.save(contact);

        // Create a timeline entry for the contact
        timelineService.createContactCreatedEntry(contact.getId(), ownerId);

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

        timelineService.createStageChangedEntry(contact.getId(), contact.getStage().name(), contactDTO.getStage().name(), ownerId);
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
        timelineService.createActivityLoggedEntry(contact.getId(), "Delete", "Deleted contact", ownerId);
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
    public void addTagToContact(Long contactId, Long tagId, String ownerId) {
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
    public void removeTagFromContact(Long contactId, Long tagId, String ownerId) {
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
    public List<TagDTO> getContactTags(Long contactId, String ownerId) {
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
    public void addNoteToContact(Long contactId, String noteContent, String ownerId) {
        Contact contact = contactRepository.findById(contactId)
                .orElseThrow(() -> new ContactNotFoundException("Contact not found with id: " + contactId));

        if (!contact.getOwnerId().equals(ownerId)) {
            throw new ContactNotFoundException("Contact not found with id: " + contactId);
        }

        Note note = new Note(noteContent, ownerId);
        note.setContact(contact);
        contact.addNote(note);
        contactRepository.save(contact);
    }

    @Override
    @Transactional
    public void removeNoteFromContact(Long contactId, Long noteId, String ownerId) {
        Contact contact = contactRepository.findById(contactId)
                .orElseThrow(() -> new ContactNotFoundException("Contact not found with id: " + contactId));

        if (!contact.getOwnerId().equals(ownerId)) {
            throw new ContactNotFoundException("Contact not found with id: " + contactId);
        }

        Note note = contact.getNotes().stream()
                .filter(n -> n.getId().equals(noteId))
                .findFirst()
                .orElseThrow(() -> new NoteNotFoundException("Note not found with id: " + noteId));

        contact.removeNote(note);
        contactRepository.save(contact);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NoteDTO> getContactNotes(Long contactId, String ownerId) {
        Contact contact = contactRepository.findById(contactId)
                .orElseThrow(() -> new ContactNotFoundException("Contact not found with id: " + contactId));

        if (!contact.getOwnerId().equals(ownerId)) {
            throw new ContactNotFoundException("Contact not found with id: " + contactId);
        }

        return contact.getNotes().stream()
                .map(noteMapper::toDTO)
                .toList();
    }
} 