package com.salesflow.contact.core.controller;

import com.salesflow.contact.core.domain.Contact;
import com.salesflow.contact.core.dto.ContactDTO;
import com.salesflow.contact.core.service.ContactService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/contacts")
@RequiredArgsConstructor
@Tag(name = "Contact Management", description = "APIs for managing contacts")
public class ContactController {

    private final ContactService contactService;
    private static final String DEFAULT_OWNER_ID = "default-owner";

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new contact")
    public ContactDTO createContact(@Valid @RequestBody ContactDTO contactDTO) {
        return contactService.createContact(contactDTO, DEFAULT_OWNER_ID);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing contact")
    public ContactDTO updateContact(@PathVariable Long id,
                                  @Valid @RequestBody ContactDTO contactDTO) {
        return contactService.updateContact(id, contactDTO, DEFAULT_OWNER_ID);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a contact")
    public void deleteContact(@PathVariable Long id) {
        contactService.deleteContact(id, DEFAULT_OWNER_ID);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a contact by ID")
    public ContactDTO getContact(@PathVariable Long id) {
        return contactService.getContact(id, DEFAULT_OWNER_ID);
    }

    @GetMapping
    @Operation(summary = "List all contacts with pagination")
    public Page<ContactDTO> getContacts(Pageable pageable) {
        return contactService.getContacts(DEFAULT_OWNER_ID, pageable);
    }

    @GetMapping("/search")
    @Operation(summary = "Search contacts")
    public Page<ContactDTO> searchContacts(@RequestParam String query,
                                         Pageable pageable) {
        return contactService.searchContacts(DEFAULT_OWNER_ID, query, pageable);
    }

    @GetMapping("/stage/{stage}")
    @Operation(summary = "Get contacts by stage")
    public List<ContactDTO> getContactsByStage(@PathVariable Contact.ContactStage stage) {
        return contactService.getContactsByStage(DEFAULT_OWNER_ID, stage);
    }

    @GetMapping("/stage/{stage}/count")
    @Operation(summary = "Count contacts by stage")
    public ResponseEntity<Long> countContactsByStage(@PathVariable Contact.ContactStage stage) {
        return ResponseEntity.ok(contactService.countContactsByStage(DEFAULT_OWNER_ID, stage));
    }

    @GetMapping("/check-email")
    @Operation(summary = "Check if email exists")
    public ResponseEntity<Boolean> checkEmailExists(@RequestParam String email) {
        return ResponseEntity.ok(contactService.existsByEmail(email));
    }

    @PostMapping("/bulk")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create multiple contacts in bulk")
    public List<ContactDTO> createBulkContacts(@Valid @RequestBody List<ContactDTO> contactDTOs) {
        return contactService.createBulkContacts(contactDTOs, DEFAULT_OWNER_ID);
    }
} 