package com.salesflow.activity.controller;

import com.salesflow.activity.dto.ActivityDTO;
import com.salesflow.activity.dto.ActivityStatsDTO;
import com.salesflow.activity.dto.TimelineDTO;
import com.salesflow.activity.model.ActivityType;
import com.salesflow.activity.service.ActivityService;
import com.salesflow.activity.service.ActivityStatsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/activities")
@RequiredArgsConstructor
@Tag(name = "Activity Management", description = "APIs for managing contact activities")
public class ActivityController {
    private final ActivityService activityService;
    private final ActivityStatsService activityStatsService;

    @PostMapping
    @Operation(summary = "Create a new activity")
    public ResponseEntity<ActivityDTO> createActivity(@Valid @RequestBody ActivityDTO activityDTO) {
        return ResponseEntity.created(null).body(activityService.createActivity(activityDTO));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get activity by ID")
    public ResponseEntity<ActivityDTO> getActivity(@PathVariable UUID id) {
        return ResponseEntity.ok(activityService.getActivity(id));
    }

    @GetMapping("/contact/{contactId}")
    @Operation(summary = "Get all activities for a contact")
    public ResponseEntity<List<ActivityDTO>> getActivitiesByContact(@PathVariable UUID contactId) {
        return ResponseEntity.ok(activityService.getActivitiesByContact(contactId));
    }

    @GetMapping("/contact/{contactId}/type/{type}")
    @Operation(summary = "Get activities by type for a contact")
    public ResponseEntity<List<ActivityDTO>> getActivitiesByType(
            @PathVariable UUID contactId,
            @PathVariable ActivityType type) {
        return ResponseEntity.ok(activityService.getActivitiesByType(contactId, type));
    }

    @GetMapping("/pending/{assignedTo}")
    @Operation(summary = "Get pending activities for a user")
    public ResponseEntity<List<ActivityDTO>> getPendingActivities(@PathVariable String assignedTo) {
        return ResponseEntity.ok(activityService.getPendingActivities(assignedTo));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an activity")
    public ResponseEntity<ActivityDTO> updateActivity(
            @PathVariable UUID id,
            @Valid @RequestBody ActivityDTO activityDTO) {
        return ResponseEntity.ok(activityService.updateActivity(id, activityDTO));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an activity")
    public ResponseEntity<Void> deleteActivity(@PathVariable UUID id) {
        activityService.deleteActivity(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/contact/{contactId}/stats")
    @Operation(summary = "Get activity statistics for a contact")
    public ResponseEntity<ActivityStatsDTO> getActivityStats(@PathVariable UUID contactId) {
        return ResponseEntity.ok(activityStatsService.getActivityStats(contactId));
    }

    @GetMapping("/contact/{contactId}/timeline")
    @Operation(summary = "Get contact timeline")
    public ResponseEntity<TimelineDTO> getContactTimeline(
            @PathVariable UUID contactId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate) {
        return ResponseEntity.ok(activityService.getContactTimeline(contactId, startDate));
    }

    @GetMapping("/search")
    @Operation(summary = "Search activities")
    public ResponseEntity<List<ActivityDTO>> searchActivities(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) ActivityType type,
            @RequestParam(required = false) String assignedTo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        return ResponseEntity.ok(activityService.searchActivities(query, type, assignedTo, startDate, endDate));
    }

    @PostMapping("/{id}/complete")
    @Operation(summary = "Mark an activity as completed")
    public ResponseEntity<ActivityDTO> completeActivity(@PathVariable UUID id) {
        return ResponseEntity.ok(activityService.completeActivity(id));
    }
} 