package com.salesflow.contact.service.impl;

import com.salesflow.contact.domain.Contact;
import com.salesflow.contact.domain.Note;
import com.salesflow.contact.dto.NoteDTO;
import com.salesflow.contact.exception.ContactNotFoundException;
import com.salesflow.contact.exception.NoteNotFoundException;
import com.salesflow.contact.mapper.NoteMapper;
import com.salesflow.contact.repository.ContactRepository;
import com.salesflow.contact.repository.NoteRepository;
import com.salesflow.contact.service.NoteService;
import com.salesflow.contact.service.TimelineService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NoteServiceImpl implements NoteService {

    private final NoteRepository noteRepository;
    private final ContactRepository contactRepository;
    private final NoteMapper noteMapper;
    private final TimelineService timelineService;

    @Override
    @Transactional
    public NoteDTO createNote(NoteDTO noteDTO, String userId) {
        Contact contact = contactRepository.findById(noteDTO.getContactId())
                .orElseThrow(() -> new ContactNotFoundException("Contact not found with id: " + noteDTO.getContactId()));

        if (!contact.getOwnerId().equals(userId)) {
            throw new ContactNotFoundException("Contact not found with id: " + noteDTO.getContactId());
        }

        Note note = noteMapper.toEntity(noteDTO);
        note.setContact(contact);
        note.setCreatedBy(userId);
        note.setUpdatedBy(userId);
        note = noteRepository.save(note);

        timelineService.createNoteAddedEntry(contact.getId(), note.getContent(), userId);
        return noteMapper.toDTO(note);
    }

    @Override
    @Transactional
    public NoteDTO updateNote(UUID id, NoteDTO noteDTO, String userId) {
        Note note = noteRepository.findById(id)
                .orElseThrow(() -> new NoteNotFoundException("Note not found with id: " + id));

        if (!note.getContact().getOwnerId().equals(userId)) {
            throw new NoteNotFoundException("Note not found with id: " + id);
        }

        noteMapper.updateEntityFromDTO(noteDTO, note);
        note.setUpdatedBy(userId);
        note = noteRepository.save(note);
        return noteMapper.toDTO(note);
    }

    @Override
    @Transactional
    public void deleteNote(UUID id, String userId) {
        Note note = noteRepository.findById(id)
                .orElseThrow(() -> new NoteNotFoundException("Note not found with id: " + id));

        if (!note.getContact().getOwnerId().equals(userId)) {
            throw new NoteNotFoundException("Note not found with id: " + id);
        }

        noteRepository.delete(note);
    }

    @Override
    @Transactional(readOnly = true)
    public NoteDTO getNote(UUID id, String userId) {
        Note note = noteRepository.findById(id)
                .orElseThrow(() -> new NoteNotFoundException("Note not found with id: " + id));

        if (!note.getContact().getOwnerId().equals(userId)) {
            throw new NoteNotFoundException("Note not found with id: " + id);
        }

        return noteMapper.toDTO(note);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NoteDTO> getContactNotes(UUID contactId, String userId) {
        Contact contact = contactRepository.findById(contactId)
                .orElseThrow(() -> new ContactNotFoundException("Contact not found with id: " + contactId));

        if (!contact.getOwnerId().equals(userId)) {
            throw new ContactNotFoundException("Contact not found with id: " + contactId);
        }

        return noteRepository.findByContactIdOrderByCreatedAtDesc(contactId)
                .stream()
                .map(noteMapper::toDTO)
                .toList();
    }

    @Override
    @Transactional
    public void deleteContactNotes(UUID contactId, String userId) {
        Contact contact = contactRepository.findById(contactId)
                .orElseThrow(() -> new ContactNotFoundException("Contact not found with id: " + contactId));

        if (!contact.getOwnerId().equals(userId)) {
            throw new ContactNotFoundException("Contact not found with id: " + contactId);
        }

        noteRepository.deleteByContactId(contactId);
    }
} 