package com.salesflow.contact.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.Instant;

@Entity
@Table(name = "notes")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Note {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Unique identifier for the note

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content; // The content of the note

    @Column(name = "created_by", nullable = false)
    private String createdBy; // The user who created the note

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt; // Timestamp for when the note was created

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contact_id", nullable = false)
    private Contact contact; // The contact associated with this note

    public Note(String content, String createdBy) {
        this.content = content;
        this.createdBy = createdBy;
        this.createdAt = Instant.now(); // Set the createdAt timestamp to now
    }
}