package com.salesflow.contact.core.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "contacts")
@Getter
@Setter
@SuperBuilder
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

    @Embedded
    private ContactPreferences preferences;

    @ElementCollection
    @CollectionTable(name = "contact_addresses", joinColumns = @JoinColumn(name = "contact_id"))
    private Set<Address> addresses = new HashSet<>();

    @ElementCollection
    @CollectionTable(name = "contact_social_profiles", joinColumns = @JoinColumn(name = "contact_id"))
    private Set<SocialProfile> socialProfiles = new HashSet<>();

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Version
    private Long version;

    public Contact() {
        // Default constructor required by JPA
    }

    public enum ContactStage {
        LEAD,
        PROSPECT,
        CUSTOMER,
        INACTIVE
    }
} 