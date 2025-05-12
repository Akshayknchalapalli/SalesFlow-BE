package com.salesflow.contact.domain;

import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;

@Embeddable
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SocialProfile {
    public enum Platform {
        LINKEDIN, TWITTER, FACEBOOK, INSTAGRAM, GITHUB, OTHER
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "platform")
    private Platform platform;
    private String profileUrl;
    private String username;
    @Column(name = "is_verified")
    private boolean verified;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "created_by", nullable = false, updatable = false)
    private String createdBy;

    @Column(name = "updated_by", nullable = false)
    private String updatedBy;

    @Column(name = "version", nullable = false)
    private Long version;

    public boolean isVerified() {
        return verified;
    }
} 