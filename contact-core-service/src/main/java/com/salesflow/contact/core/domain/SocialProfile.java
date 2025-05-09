package com.salesflow.contact.core.domain;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.Column;

@Embeddable
@Getter
@Setter
public class SocialProfile {
    
    @Column(name = "platform")
    private String platform; // e.g., LINKEDIN, TWITTER, FACEBOOK
    
    @Column(name = "profile_url")
    private String profileUrl;
    
    @Column(name = "username")
    private String username;
    
    @Column(name = "is_verified")
    private boolean verified;
} 