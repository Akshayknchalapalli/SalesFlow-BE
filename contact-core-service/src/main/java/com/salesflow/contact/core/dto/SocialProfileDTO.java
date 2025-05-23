package com.salesflow.contact.core.dto;

import lombok.Data;

@Data
public class SocialProfileDTO {
    private String platform;
    private String profileUrl;
    private String username;
    private boolean verified;
} 