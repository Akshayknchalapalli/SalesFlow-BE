package com.salesflow.activity.controller;

import com.salesflow.activity.dto.ActivityDTO;
import com.salesflow.activity.dto.ActivityStatsDTO;
import com.salesflow.activity.dto.ApiResponse;
import com.salesflow.activity.dto.TimelineDTO;
import com.salesflow.activity.model.ActivityType;
import com.salesflow.activity.service.ActivityService;
import com.salesflow.activity.service.ActivityStatsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/activities")
@RequiredArgsConstructor
@Tag(name = "Activity Management", description = "APIs for managing contact activities")
@Slf4j
public class ActivityController {
    private final ActivityService activityService;
    private final ActivityStatsService activityStatsService;

    @PostMapping
    @Operation(summary = "Create a new activity")
    public ResponseEntity<ApiResponse<ActivityDTO>> createActivity(@Valid @RequestBody ActivityDTO activityDTO) {
        log.debug("Received request to create activity: {}", activityDTO);
        ApiResponse<ActivityDTO> response = activityService.createActivity(activityDTO);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get activity by ID")
    public ResponseEntity<ApiResponse<ActivityDTO>> getActivity(
            @Parameter(description = "Activity ID") @PathVariable UUID id) {
        ApiResponse<ActivityDTO> response = activityService.getActivity(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/contact/{contactId}")
    @Operation(summary = "Get all activities for a contact")
    public ResponseEntity<ApiResponse<List<ActivityDTO>>> getActivitiesByContact(
            @Parameter(description = "Contact ID") @PathVariable UUID contactId) {
        ApiResponse<List<ActivityDTO>> response = activityService.getActivitiesByContact(contactId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/contact/{contactId}/type/{type}")
    @Operation(summary = "Get activities by type for a contact")
    public ResponseEntity<ApiResponse<List<ActivityDTO>>> getActivitiesByType(
            @Parameter(description = "Contact ID") @PathVariable UUID contactId,
            @Parameter(description = "Activity type") @PathVariable ActivityType type) {
        ApiResponse<List<ActivityDTO>> response = activityService.getActivitiesByType(contactId, type);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/pending")
    @Operation(summary = "Get pending activities for a user")
    public ResponseEntity<ApiResponse<List<ActivityDTO>>> getPendingActivities(
            @Parameter(description = "Assigned user") @RequestParam String assignedTo) {
        ApiResponse<List<ActivityDTO>> response = activityService.getPendingActivities(assignedTo);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an activity")
    public ResponseEntity<ApiResponse<ActivityDTO>> updateActivity(
            @Parameter(description = "Activity ID") @PathVariable UUID id,
            @Valid @RequestBody ActivityDTO activityDTO) {
        ApiResponse<ActivityDTO> response = activityService.updateActivity(id, activityDTO);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an activity")
    public ResponseEntity<ApiResponse<Void>> deleteActivity(
            @Parameter(description = "Activity ID") @PathVariable UUID id) {
        ApiResponse<Void> response = activityService.deleteActivity(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/contact/{contactId}/stats")
    @Operation(summary = "Get activity statistics for a contact")
    public ResponseEntity<ActivityStatsDTO> getActivityStats(@PathVariable UUID contactId) {
        return ResponseEntity.ok(activityStatsService.getActivityStats(contactId));
    }

    @GetMapping("/contact/{contactId}/timeline")
    @Operation(summary = "Get contact timeline")
    public ResponseEntity<ApiResponse<TimelineDTO>> getContactTimeline(
            @Parameter(description = "Contact ID") @PathVariable UUID contactId,
            @Parameter(description = "Start date") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate) {
        ApiResponse<TimelineDTO> response = activityService.getContactTimeline(contactId, startDate);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    @Operation(summary = "Search activities")
    public ResponseEntity<ApiResponse<List<ActivityDTO>>> searchActivities(
            @Parameter(description = "Search query") @RequestParam(required = false) String query,
            @Parameter(description = "Activity type") @RequestParam(required = false) ActivityType type,
            @Parameter(description = "Assigned user") @RequestParam(required = false) String assignedTo,
            @Parameter(description = "Start date") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "End date") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        ApiResponse<List<ActivityDTO>> response = activityService.searchActivities(query, type, assignedTo, startDate, endDate);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/complete")
    @Operation(summary = "Mark activity as completed")
    public ResponseEntity<ApiResponse<ActivityDTO>> completeActivity(
            @Parameter(description = "Activity ID") @PathVariable UUID id) {
        ApiResponse<ActivityDTO> response = activityService.completeActivity(id);
        return ResponseEntity.ok(response);
    }
} 