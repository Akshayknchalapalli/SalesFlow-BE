package com.salesflow.contact.domain;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name = "contacts")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Contact extends BaseEntity {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

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
    private Set<Address> addresses = new HashSet<>();

    @ElementCollection
    @CollectionTable(name = "contact_social_profiles", joinColumns = @JoinColumn(name = "contact_id"))
    private Set<SocialProfile> socialProfiles = new HashSet<>();

    @OneToMany(mappedBy = "contact", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<TimelineEntry> timelineEntries = new HashSet<>();

    @OneToMany(mappedBy = "contact", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Note> notes = new HashSet<>();

    @ManyToMany
    @JoinTable(
        name = "contact_tags",
        joinColumns = @JoinColumn(name = "contact_id"),
        inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> tags = new HashSet<>();

    @ManyToMany
    @JoinTable(
        name = "contact_related_contacts",
        joinColumns = @JoinColumn(name = "contact_id"),
        inverseJoinColumns = @JoinColumn(name = "related_contact_id")
    )
    private Set<Contact> relatedContacts = new HashSet<>();

    @Version
    private Long version;

    public enum ContactStage {
        LEAD,
        PROSPECT,
        CUSTOMER,
        INACTIVE,
        PARTNER
    }

    public enum ContactMethod {
        EMAIL, PHONE, SMS, MAIL
    }

    public enum ContactTime {
        MORNING, AFTERNOON, EVENING, ANYTIME
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

    public void addRelatedContact(Contact contact) {
        relatedContacts.add(contact);
    }

    public void removeRelatedContact(Contact contact) {
        relatedContacts.remove(contact);
    }
} 