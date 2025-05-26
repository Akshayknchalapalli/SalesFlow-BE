package com.salesflow.contact.domain;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import java.time.Instant;

@Embeddable
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SocialProfile {
    
    public enum Platform {
        LINKEDIN,
        TWITTER,
        FACEBOOK,
        INSTAGRAM,
        GITHUB,
        WEBSITE,
        OTHER
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "platform", nullable = false)
    private Platform platform;

    @Column(name = "profile_url", nullable = false)
    private String profileUrl;

    @Column(name = "is_primary")
    private boolean primary;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "created_by", nullable = false, updatable = false)
    private String createdBy;

    @Column(name = "updated_by", nullable = false)
    private String updatedBy;
} 