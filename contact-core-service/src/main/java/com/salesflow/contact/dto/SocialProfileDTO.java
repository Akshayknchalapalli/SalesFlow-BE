package com.salesflow.contact.dto;

import lombok.Data;

@Data
public class SocialProfileDTO {
    private String platform;
    private String profileUrl;
    private String username;
    private boolean verified;
} 