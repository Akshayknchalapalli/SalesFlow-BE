package com.salesflow.activity.controller;

import com.salesflow.activity.dto.ActivityDTO;
import com.salesflow.activity.dto.ActivityStatsDTO;
import com.salesflow.activity.dto.TimelineDTO;
import com.salesflow.activity.model.ActivityType;
import com.salesflow.activity.service.ActivityService;
import com.salesflow.activity.service.ActivityStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/activities")
@RequiredArgsConstructor
public class ActivityController {
    private final ActivityService activityService;
    private final ActivityStatsService activityStatsService;

    @PostMapping
    public ResponseEntity<ActivityDTO> createActivity(@Valid @RequestBody ActivityDTO activityDTO) {
        return ResponseEntity.ok(activityService.createActivity(activityDTO));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ActivityDTO> getActivity(@PathVariable Long id) {
        return ResponseEntity.ok(activityService.getActivity(id));
    }

    @GetMapping("/contact/{contactId}")
    public ResponseEntity<List<ActivityDTO>> getActivitiesByContact(@PathVariable Long contactId) {
        return ResponseEntity.ok(activityService.getActivitiesByContact(contactId));
    }

    @GetMapping("/contact/{contactId}/type/{type}")
    public ResponseEntity<List<ActivityDTO>> getActivitiesByType(
            @PathVariable Long contactId,
            @PathVariable ActivityType type) {
        return ResponseEntity.ok(activityService.getActivitiesByType(contactId, type));
    }

    @GetMapping("/pending/{assignedTo}")
    public ResponseEntity<List<ActivityDTO>> getPendingActivities(@PathVariable String assignedTo) {
        return ResponseEntity.ok(activityService.getPendingActivities(assignedTo));
    }

    @PutMapping("/{id}/complete")
    public ResponseEntity<ActivityDTO> completeActivity(@PathVariable Long id) {
        return ResponseEntity.ok(activityService.completeActivity(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ActivityDTO> updateActivity(
            @PathVariable Long id,
            @Valid @RequestBody ActivityDTO activityDTO) {
        return ResponseEntity.ok(activityService.updateActivity(id, activityDTO));
    }

    @GetMapping("/contact/{contactId}/stats")
    public ResponseEntity<ActivityStatsDTO> getContactActivityStats(@PathVariable Long contactId) {
        return ResponseEntity.ok(activityStatsService.getContactActivityStats(contactId));
    }

    @GetMapping("/contact/{contactId}/timeline")
    public ResponseEntity<List<TimelineDTO>> getContactTimeline(
            @PathVariable Long contactId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        return ResponseEntity.ok(activityService.getContactTimeline(contactId, startDate, endDate));
    }

    @GetMapping("/search")
    public ResponseEntity<List<ActivityDTO>> searchActivities(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) ActivityType type,
            @RequestParam(required = false) String assignedTo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        return ResponseEntity.ok(activityService.searchActivities(query, type, assignedTo, startDate, endDate));
    }
} 