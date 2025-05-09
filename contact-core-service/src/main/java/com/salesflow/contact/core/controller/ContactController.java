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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/contacts")
@RequiredArgsConstructor
@Tag(name = "Contact Management", description = "APIs for managing contacts")
public class ContactController {

    private final ContactService contactService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new contact")
    public ContactDTO createContact(@Valid @RequestBody ContactDTO contactDTO,
                                  @AuthenticationPrincipal Jwt jwt) {
        return contactService.createContact(contactDTO, jwt.getSubject());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing contact")
    public ContactDTO updateContact(@PathVariable Long id,
                                  @Valid @RequestBody ContactDTO contactDTO,
                                  @AuthenticationPrincipal Jwt jwt) {
        return contactService.updateContact(id, contactDTO, jwt.getSubject());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a contact")
    public void deleteContact(@PathVariable Long id,
                            @AuthenticationPrincipal Jwt jwt) {
        contactService.deleteContact(id, jwt.getSubject());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a contact by ID")
    public ContactDTO getContact(@PathVariable Long id,
                               @AuthenticationPrincipal Jwt jwt) {
        return contactService.getContact(id, jwt.getSubject());
    }

    @GetMapping
    @Operation(summary = "List all contacts with pagination")
    public Page<ContactDTO> getContacts(@AuthenticationPrincipal Jwt jwt,
                                      Pageable pageable) {
        return contactService.getContacts(jwt.getSubject(), pageable);
    }

    @GetMapping("/search")
    @Operation(summary = "Search contacts")
    public Page<ContactDTO> searchContacts(@RequestParam String query,
                                         @AuthenticationPrincipal Jwt jwt,
                                         Pageable pageable) {
        return contactService.searchContacts(jwt.getSubject(), query, pageable);
    }

    @GetMapping("/stage/{stage}")
    @Operation(summary = "Get contacts by stage")
    public List<ContactDTO> getContactsByStage(@PathVariable Contact.ContactStage stage,
                                             @AuthenticationPrincipal Jwt jwt) {
        return contactService.getContactsByStage(jwt.getSubject(), stage);
    }

    @GetMapping("/stage/{stage}/count")
    @Operation(summary = "Count contacts by stage")
    public ResponseEntity<Long> countContactsByStage(@PathVariable Contact.ContactStage stage,
                                                   @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(contactService.countContactsByStage(jwt.getSubject(), stage));
    }

    @GetMapping("/check-email")
    @Operation(summary = "Check if email exists")
    public ResponseEntity<Boolean> checkEmailExists(@RequestParam String email) {
        return ResponseEntity.ok(contactService.existsByEmail(email));
    }
} 