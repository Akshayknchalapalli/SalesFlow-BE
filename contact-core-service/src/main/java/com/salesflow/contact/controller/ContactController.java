package com.salesflow.contact.controller;

import com.salesflow.contact.dto.ContactDTO;
import com.salesflow.contact.dto.ContactApiResponse;
import com.salesflow.contact.service.ContactService;
import com.salesflow.contact.domain.Contact;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/contacts")
@RequiredArgsConstructor
@Tag(name = "Contact Management", description = "APIs for managing contacts in the SalesFlow system")
public class ContactController {
    private final ContactService contactService;

    @Operation(
        summary = "Create a new contact",
        description = """
            Creates a new contact with the provided details.
            
            ## Required Fields
            - firstName: First name of the contact
            - lastName: Last name of the contact
            - stage: Contact stage (LEAD, PROSPECT, CUSTOMER, INACTIVE, PARTNER)
            - ownerId: ID of the contact owner
            
            ## Optional Fields
            - email: Email address (must be unique if provided)
            - phone: Phone number
            - companyName: Company name
            - jobTitle: Job title
            - teamId: Team ID
            - regionId: Region ID
            - preferences: Contact preferences
            - addresses: List of addresses
            - socialProfiles: List of social profiles
            
            ## Example Request
            ```json
            {
              "firstName": "John",
              "lastName": "Doe",
              "email": "john.doe@example.com",
              "phone": "+1234567890",
              "companyName": "Acme Corp",
              "jobTitle": "Sales Manager",
              "stage": "LEAD",
              "ownerId": "user123",
              "preferences": {
                "preferredContactMethod": "EMAIL",
                "preferredContactTime": "MORNING",
                "doNotContact": false,
                "marketingOptIn": true
              }
            }
            ```
            """
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Contact created successfully",
            content = @Content(schema = @Schema(implementation = ContactApiResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input - Check the request body for validation errors"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Missing or invalid X-Owner-Id header"),
        @ApiResponse(responseCode = "409", description = "Conflict - Email already exists")
    })
    @PostMapping
    public ResponseEntity<ContactApiResponse<ContactDTO>> createContact(
            @RequestBody ContactDTO contactDTO,
            @Parameter(description = "Owner ID of the contact", required = true)
            @RequestHeader("X-Owner-Id") String ownerId) {
        ContactDTO createdContact = contactService.createContact(contactDTO, ownerId);
        return ResponseEntity.ok(ContactApiResponse.success("Contact created successfully", createdContact));
    }

    @Operation(summary = "Get a contact by ID", description = "Retrieves a contact's details by their ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Contact found",
            content = @Content(schema = @Schema(implementation = ContactApiResponse.class))),
        @ApiResponse(responseCode = "404", description = "Contact not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ContactApiResponse<ContactDTO>> getContact(
            @Parameter(description = "ID of the contact to retrieve", required = true)
            @PathVariable UUID id,
            @Parameter(description = "Owner ID of the contact", required = true)
            @RequestHeader("X-Owner-Id") String ownerId) {
        ContactDTO contact = contactService.getContact(id, ownerId);
        return ResponseEntity.ok(ContactApiResponse.success("Contact retrieved successfully", contact));
    }

    @Operation(summary = "Update a contact", description = "Updates an existing contact's details")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Contact updated successfully",
            content = @Content(schema = @Schema(implementation = ContactApiResponse.class))),
        @ApiResponse(responseCode = "404", description = "Contact not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ContactApiResponse<ContactDTO>> updateContact(
            @Parameter(description = "ID of the contact to update", required = true)
            @PathVariable UUID id,
            @RequestBody ContactDTO contactDTO,
            @Parameter(description = "Owner ID of the contact", required = true)
            @RequestHeader("X-Owner-Id") String ownerId) {
        ContactDTO updatedContact = contactService.updateContact(id, contactDTO, ownerId);
        return ResponseEntity.ok(ContactApiResponse.success("Contact updated successfully", updatedContact));
    }

    @Operation(summary = "Delete a contact", description = "Deletes a contact by their ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Contact deleted successfully",
            content = @Content(schema = @Schema(implementation = ContactApiResponse.class))),
        @ApiResponse(responseCode = "404", description = "Contact not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<ContactApiResponse<Void>> deleteContact(
            @Parameter(description = "ID of the contact to delete", required = true)
            @PathVariable UUID id,
            @Parameter(description = "Owner ID of the contact", required = true)
            @RequestHeader("X-Owner-Id") String ownerId) {
        contactService.deleteContact(id, ownerId);
        return ResponseEntity.ok(ContactApiResponse.success("Contact deleted successfully", null));
    }

    @Operation(summary = "List contacts", description = "Retrieves a paginated list of contacts")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Contacts retrieved successfully",
            content = @Content(schema = @Schema(implementation = ContactApiResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping
    public ResponseEntity<ContactApiResponse<Page<ContactDTO>>> listContacts(
            @Parameter(description = "Page number (0-based)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of items per page")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Owner ID of the contacts", required = true)
            @RequestHeader("X-Owner-Id") String ownerId) {
        Page<ContactDTO> contacts = contactService.getContacts(ownerId, PageRequest.of(page, size));
        return ResponseEntity.ok(ContactApiResponse.success("Contacts retrieved successfully", contacts));
    }

    @Operation(summary = "Search contacts", description = "Searches contacts based on a search term")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Search results retrieved successfully",
            content = @Content(schema = @Schema(implementation = ContactApiResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/search")
    public ResponseEntity<ContactApiResponse<Page<ContactDTO>>> searchContacts(
            @Parameter(description = "Search term to look for in contacts", required = true)
            @RequestParam String searchTerm,
            @Parameter(description = "Page number (0-based)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of items per page")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Owner ID of the contacts", required = true)
            @RequestHeader("X-Owner-Id") String ownerId) {
        Page<ContactDTO> contacts = contactService.searchContacts(ownerId, searchTerm, PageRequest.of(page, size));
        return ResponseEntity.ok(ContactApiResponse.success("Search results retrieved successfully", contacts));
    }

    @Operation(summary = "Get contacts by stage", description = "Retrieves all contacts in a specific stage")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Contacts retrieved successfully",
            content = @Content(schema = @Schema(implementation = ContactApiResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/stage/{stage}")
    public ResponseEntity<ContactApiResponse<List<ContactDTO>>> getContactsByStage(
            @Parameter(description = "Stage of the contacts to retrieve", required = true)
            @PathVariable String stage,
            @Parameter(description = "Owner ID of the contacts", required = true)
            @RequestHeader("X-Owner-Id") String ownerId) {
        List<ContactDTO> contacts = contactService.getContactsByStage(ownerId, Contact.ContactStage.valueOf(stage));
        return ResponseEntity.ok(ContactApiResponse.success("Contacts retrieved successfully", contacts));
    }

    @Operation(summary = "Count contacts by stage", description = "Counts the number of contacts in a specific stage")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Count retrieved successfully",
            content = @Content(schema = @Schema(implementation = ContactApiResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/stage/{stage}/count")
    public ResponseEntity<ContactApiResponse<Long>> countContactsByStage(
            @Parameter(description = "Stage of the contacts to count", required = true)
            @PathVariable String stage,
            @Parameter(description = "Owner ID of the contacts", required = true)
            @RequestHeader("X-Owner-Id") String ownerId) {
        Long count = contactService.countContactsByStage(ownerId, Contact.ContactStage.valueOf(stage));
        return ResponseEntity.ok(ContactApiResponse.success("Count retrieved successfully", count));
    }

    @Operation(summary = "Check if contact exists", description = "Checks if a contact exists with the given email")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Check completed successfully",
            content = @Content(schema = @Schema(implementation = ContactApiResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/exists")
    public ResponseEntity<ContactApiResponse<Boolean>> existsByEmail(
            @Parameter(description = "Email to check", required = true)
            @RequestParam String email) {
        boolean exists = contactService.existsByEmail(email);
        return ResponseEntity.ok(ContactApiResponse.success("Check completed successfully", exists));
    }

    @Operation(summary = "Create multiple contacts", description = "Creates multiple contacts in bulk")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Contacts created successfully",
            content = @Content(schema = @Schema(implementation = ContactApiResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PostMapping("/bulk")
    public ResponseEntity<ContactApiResponse<List<ContactDTO>>> createBulkContacts(
            @RequestBody List<ContactDTO> contactDTOs,
            @Parameter(description = "Owner ID of the contacts", required = true)
            @RequestHeader("X-Owner-Id") String ownerId) {
        List<ContactDTO> createdContacts = contactService.createBulkContacts(contactDTOs, ownerId);
        return ResponseEntity.ok(ContactApiResponse.success("Contacts created successfully", createdContacts));
    }

    @GetMapping("/test-security")
    @Operation(summary = "Test security integration", description = "This endpoint is secured and requires a valid JWT token")
    public ResponseEntity<ContactApiResponse<String>> testSecurity() {
        return ResponseEntity.ok(ContactApiResponse.success("Security test successful", "You have access to this secured endpoint!"));
    }
} 