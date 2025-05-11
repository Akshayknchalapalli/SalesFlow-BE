package com.salesflow.contact.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "contacts")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Contact extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(unique = true)
    private String email;

    private String phone;

    @Column(name = "company_name")
    private String companyName;

    @Column(name = "job_title")
    private String jobTitle;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ContactStage stage;

    @Column(name = "owner_id", nullable = false)
    private String ownerId;

    @Column(name = "team_id")
    private String teamId;

    @Column(name = "region_id")
    private String regionId;

    @Embedded
    private ContactPreferences preferences;

    @ElementCollection
    @CollectionTable(name = "contact_addresses", joinColumns = @JoinColumn(name = "contact_id"))
    @Builder.Default
    private Set<Address> addresses = new HashSet<>();

    @ElementCollection
    @CollectionTable(name = "contact_social_profiles", joinColumns = @JoinColumn(name = "contact_id"))
    @Builder.Default
    private Set<SocialProfile> socialProfiles = new HashSet<>();

    @ManyToMany
    @JoinTable(
        name = "contact_tags",
        joinColumns = @JoinColumn(name = "contact_id"),
        inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    @Builder.Default
    private Set<Tag> tags = new HashSet<>();

    @OneToMany(mappedBy = "contact", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<TimelineEntry> timelineEntries = new HashSet<>();

    @OneToMany(mappedBy = "contact", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<Note> notes = new HashSet<>();

    @Version
    private Long version;

    public enum ContactStage {
        LEAD,
        PROSPECT,
        CUSTOMER,
        INACTIVE,
        PARTNER
    }

    public void addTimelineEntry(TimelineEntry entry) {
        timelineEntries.add(entry);
        entry.setContact(this);
    }

    public void removeTimelineEntry(TimelineEntry entry) {
        timelineEntries.remove(entry);
        entry.setContact(null);
    }

    public void addTag(Tag tag) {
        tags.add(tag);
    }

    public void removeTag(Tag tag) {
        tags.remove(tag);
    }

    public void addNote(Note note) {
        notes.add(note);
        note.setContact(this);
    }

    public void removeNote(Note note) {
        notes.remove(note);
        note.setContact(null);
    }
    
    
} 