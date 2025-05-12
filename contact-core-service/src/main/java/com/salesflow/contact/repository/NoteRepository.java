package com.salesflow.contact.repository;

import com.salesflow.contact.domain.Note;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NoteRepository extends JpaRepository<Note, UUID> {
    List<Note> findByContactIdOrderByCreatedAtDesc(UUID contactId);
    void deleteByContactId(UUID contactId);
    boolean existsByIdAndContactId(UUID id, UUID contactId);
} 