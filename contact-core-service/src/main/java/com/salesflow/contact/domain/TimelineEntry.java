package com.salesflow.contact.domain;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import java.util.UUID;

@Entity
@Table(name = "timeline_entries", schema = "contact_data")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class TimelineEntry extends BaseEntity {
    
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contact_id", nullable = false)
    private Contact contact;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EntryType type;
    
    @Column(nullable = false)
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "owner_id", nullable = false)
    private String ownerId;
    
    @Version
    private Long version;
    
    public enum EntryType {
        CONTACT_CREATED,
        CONTACT_UPDATED,
        CONTACT_DELETED,
        STAGE_CHANGED,
        TAG_ADDED,
        TAG_REMOVED,
        NOTE_ADDED,
        NOTE_UPDATED,
        NOTE_DELETED,
        ACTIVITY_LOGGED,
        OWNERSHIP_CHANGED
    }
} 