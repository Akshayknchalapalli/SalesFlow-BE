package com.salesflow.contact.controller;

import com.salesflow.contact.dto.ContactDTO;
import com.salesflow.contact.service.ContactService;
import com.salesflow.contact.domain.Contact;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/contacts")
@RequiredArgsConstructor
public class ContactController {
    private final ContactService contactService;

    @PostMapping
    public ResponseEntity<ContactDTO> createContact(
            @RequestBody ContactDTO contactDTO,
            @RequestHeader("X-Owner-Id") String ownerId) {
        return ResponseEntity.ok(contactService.createContact(contactDTO, ownerId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ContactDTO> getContact(
            @PathVariable Long id,
            @RequestHeader("X-Owner-Id") String ownerId) {
        return ResponseEntity.ok(contactService.getContact(id, ownerId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ContactDTO> updateContact(
            @PathVariable Long id,
            @RequestBody ContactDTO contactDTO,
            @RequestHeader("X-Owner-Id") String ownerId) {
        return ResponseEntity.ok(contactService.updateContact(id, contactDTO, ownerId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteContact(
            @PathVariable Long id,
            @RequestHeader("X-Owner-Id") String ownerId) {
        contactService.deleteContact(id, ownerId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<Page<ContactDTO>> listContacts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestHeader("X-Owner-Id") String ownerId) {
        return ResponseEntity.ok(contactService.getContacts(ownerId, PageRequest.of(page, size)));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<ContactDTO>> searchContacts(
            @RequestParam String searchTerm,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestHeader("X-Owner-Id") String ownerId) {
        return ResponseEntity.ok(contactService.searchContacts(ownerId, searchTerm, PageRequest.of(page, size)));
    }

    @GetMapping("/stage/{stage}")
    public ResponseEntity<List<ContactDTO>> getContactsByStage(
            @PathVariable String stage,
            @RequestHeader("X-Owner-Id") String ownerId) {
        return ResponseEntity.ok(contactService.getContactsByStage(ownerId, Contact.ContactStage.valueOf(stage)));
    }

    @GetMapping("/stage/{stage}/count")
    public ResponseEntity<Long> countContactsByStage(
            @PathVariable String stage,
            @RequestHeader("X-Owner-Id") String ownerId) {
        return ResponseEntity.ok(contactService.countContactsByStage(ownerId, Contact.ContactStage.valueOf(stage)));
    }

    @GetMapping("/exists")
    public ResponseEntity<Boolean> existsByEmail(@RequestParam String email) {
        return ResponseEntity.ok(contactService.existsByEmail(email));
    }

    @PostMapping("/bulk")
    public ResponseEntity<List<ContactDTO>> createBulkContacts(
            @RequestBody List<ContactDTO> contactDTOs,
            @RequestHeader("X-Owner-Id") String ownerId) {
        return ResponseEntity.ok(contactService.createBulkContacts(contactDTOs, ownerId));
    }
} 