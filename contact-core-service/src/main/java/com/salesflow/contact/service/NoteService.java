package com.salesflow.contact.service;

import com.salesflow.contact.dto.NoteDTO;
import java.util.List;
import java.util.UUID;

public interface NoteService {
    NoteDTO createNote(NoteDTO noteDTO, String userId);
    NoteDTO updateNote(UUID id, NoteDTO noteDTO, String userId);
    void deleteNote(UUID id, String userId);
    NoteDTO getNote(UUID id, String userId);
    List<NoteDTO> getContactNotes(UUID contactId, String userId);
    void deleteContactNotes(UUID contactId, String userId);
} 