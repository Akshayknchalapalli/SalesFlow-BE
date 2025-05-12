package com.salesflow.contact.controller;

import com.salesflow.contact.dto.TagDTO;
import com.salesflow.contact.dto.ContactApiResponse;
import com.salesflow.contact.service.TagService;
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
@RequestMapping("/tags")
@RequiredArgsConstructor
@Tag(name = "Tag Management", description = "APIs for managing tags in the SalesFlow system")
public class TagController {
    private final TagService tagService;

    @Operation(summary = "Create a new tag", description = "Creates a new tag with the provided details")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Tag created successfully",
            content = @Content(schema = @Schema(implementation = ContactApiResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PostMapping
    public ResponseEntity<ContactApiResponse<TagDTO>> createTag(
            @RequestBody TagDTO tagDTO,
            @Parameter(description = "Owner ID of the tag", required = true)
            @RequestHeader("X-Owner-Id") String ownerId) {
        TagDTO createdTag = tagService.createTag(tagDTO, ownerId);
        return ResponseEntity.ok(ContactApiResponse.success("Tag created successfully", createdTag));
    }

    @Operation(summary = "Get a tag by ID", description = "Retrieves a tag's details by their ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Tag found",
            content = @Content(schema = @Schema(implementation = ContactApiResponse.class))),
        @ApiResponse(responseCode = "404", description = "Tag not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ContactApiResponse<TagDTO>> getTag(
            @Parameter(description = "ID of the tag to retrieve", required = true)
            @PathVariable UUID id,
            @Parameter(description = "Owner ID of the tag", required = true)
            @RequestHeader("X-Owner-Id") String ownerId) {
        TagDTO tag = tagService.getTag(id, ownerId);
        return ResponseEntity.ok(ContactApiResponse.success("Tag retrieved successfully", tag));
    }

    @Operation(summary = "Update a tag", description = "Updates an existing tag's details")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Tag updated successfully",
            content = @Content(schema = @Schema(implementation = ContactApiResponse.class))),
        @ApiResponse(responseCode = "404", description = "Tag not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ContactApiResponse<TagDTO>> updateTag(
            @Parameter(description = "ID of the tag to update", required = true)
            @PathVariable UUID id,
            @RequestBody TagDTO tagDTO,
            @Parameter(description = "Owner ID of the tag", required = true)
            @RequestHeader("X-Owner-Id") String ownerId) {
        TagDTO updatedTag = tagService.updateTag(id, tagDTO, ownerId);
        return ResponseEntity.ok(ContactApiResponse.success("Tag updated successfully", updatedTag));
    }

    @Operation(summary = "Delete a tag", description = "Deletes a tag by their ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Tag deleted successfully",
            content = @Content(schema = @Schema(implementation = ContactApiResponse.class))),
        @ApiResponse(responseCode = "404", description = "Tag not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<ContactApiResponse<Void>> deleteTag(
            @Parameter(description = "ID of the tag to delete", required = true)
            @PathVariable UUID id,
            @Parameter(description = "Owner ID of the tag", required = true)
            @RequestHeader("X-Owner-Id") String ownerId) {
        tagService.deleteTag(id, ownerId);
        return ResponseEntity.ok(ContactApiResponse.success("Tag deleted successfully", null));
    }

    @Operation(summary = "List tags", description = "Retrieves a paginated list of tags")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Tags retrieved successfully",
            content = @Content(schema = @Schema(implementation = ContactApiResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping
    public ResponseEntity<ContactApiResponse<Page<TagDTO>>> listTags(
            @Parameter(description = "Page number (0-based)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of items per page")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Owner ID of the tags", required = true)
            @RequestHeader("X-Owner-Id") String ownerId) {
        Page<TagDTO> tags = tagService.getTags(ownerId, PageRequest.of(page, size));
        return ResponseEntity.ok(ContactApiResponse.success("Tags retrieved successfully", tags));
    }

    @Operation(summary = "Search tags", description = "Searches tags based on a search term")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Search results retrieved successfully",
            content = @Content(schema = @Schema(implementation = ContactApiResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/search")
    public ResponseEntity<ContactApiResponse<Page<TagDTO>>> searchTags(
            @Parameter(description = "Search term to look for in tags", required = true)
            @RequestParam String searchTerm,
            @Parameter(description = "Page number (0-based)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of items per page")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Owner ID of the tags", required = true)
            @RequestHeader("X-Owner-Id") String ownerId) {
        Page<TagDTO> tags = tagService.searchTags(ownerId, searchTerm, PageRequest.of(page, size));
        return ResponseEntity.ok(ContactApiResponse.success("Search results retrieved successfully", tags));
    }

    @Operation(summary = "Get all tags", description = "Retrieves all tags for an owner")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Tags retrieved successfully",
            content = @Content(schema = @Schema(implementation = ContactApiResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/all")
    public ResponseEntity<ContactApiResponse<List<TagDTO>>> getAllTags(
            @Parameter(description = "Owner ID of the tags", required = true)
            @RequestHeader("X-Owner-Id") String ownerId) {
        List<TagDTO> tags = tagService.getAllTags(ownerId);
        return ResponseEntity.ok(ContactApiResponse.success("Tags retrieved successfully", tags));
    }

    @Operation(summary = "Add tag to contact", description = "Adds a tag to a contact")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Tag added successfully",
            content = @Content(schema = @Schema(implementation = ContactApiResponse.class))),
        @ApiResponse(responseCode = "404", description = "Contact or tag not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PostMapping("/{tagId}/contacts/{contactId}")
    public ResponseEntity<ContactApiResponse<Void>> addTagToContact(
            @Parameter(description = "ID of the tag to add", required = true)
            @PathVariable UUID tagId,
            @Parameter(description = "ID of the contact to add the tag to", required = true)
            @PathVariable UUID contactId,
            @Parameter(description = "Owner ID", required = true)
            @RequestHeader("X-Owner-Id") String ownerId) {
        tagService.addTagToContact(contactId, tagId, ownerId);
        return ResponseEntity.ok(ContactApiResponse.success("Tag added to contact successfully", null));
    }

    @Operation(summary = "Remove tag from contact", description = "Removes a tag from a contact")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Tag removed successfully",
            content = @Content(schema = @Schema(implementation = ContactApiResponse.class))),
        @ApiResponse(responseCode = "404", description = "Contact or tag not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @DeleteMapping("/{tagId}/contacts/{contactId}")
    public ResponseEntity<ContactApiResponse<Void>> removeTagFromContact(
            @Parameter(description = "ID of the tag to remove", required = true)
            @PathVariable UUID tagId,
            @Parameter(description = "ID of the contact to remove the tag from", required = true)
            @PathVariable UUID contactId,
            @Parameter(description = "Owner ID", required = true)
            @RequestHeader("X-Owner-Id") String ownerId) {
        tagService.removeTagFromContact(contactId, tagId, ownerId);
        return ResponseEntity.ok(ContactApiResponse.success("Tag removed from contact successfully", null));
    }

    @Operation(summary = "Get contact tags", description = "Retrieves all tags for a contact")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Tags retrieved successfully",
            content = @Content(schema = @Schema(implementation = ContactApiResponse.class))),
        @ApiResponse(responseCode = "404", description = "Contact not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/contacts/{contactId}")
    public ResponseEntity<ContactApiResponse<List<TagDTO>>> getContactTags(
            @Parameter(description = "ID of the contact to get tags for", required = true)
            @PathVariable UUID contactId,
            @Parameter(description = "Owner ID", required = true)
            @RequestHeader("X-Owner-Id") String ownerId) {
        List<TagDTO> tags = tagService.getContactTags(contactId, ownerId);
        return ResponseEntity.ok(ContactApiResponse.success("Contact tags retrieved successfully", tags));
    }
} 