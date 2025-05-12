package com.salesflow.contact.dto;

import com.salesflow.contact.domain.SocialProfile.Platform;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SocialProfileDTO {
    private Platform platform;
    private String profileUrl;
    private String username;
    private boolean verified;
    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;
    private Long version;

    public boolean isVerified() {
        return verified;
    }
} 