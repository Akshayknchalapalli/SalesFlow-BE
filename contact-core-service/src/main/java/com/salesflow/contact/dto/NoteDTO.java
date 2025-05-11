package com.salesflow.contact.dto;

import lombok.Data;

@Data
public class NoteDTO {
    private Long id; // Optional, if you want to track note IDs
    private String content; // The content of the note
    private String createdBy; // The user who created the note
    private Long contactId; // The ID of the contact this note is associated with
}
