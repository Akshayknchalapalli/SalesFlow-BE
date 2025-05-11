package com.salesflow.contact.dto;

import lombok.Data;
import java.time.Instant;

@Data
public class TagDTO {
    private Long id;
    private String name;
    private String description;
    private String colorHex;
    private String ownerId;
    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;
    private Long version;
} 