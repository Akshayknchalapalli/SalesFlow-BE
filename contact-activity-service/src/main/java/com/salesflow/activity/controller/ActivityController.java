package com.salesflow.activity.controller;

import com.salesflow.activity.dto.ActivityDTO;
import com.salesflow.activity.model.ActivityType;
import com.salesflow.activity.service.ActivityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/v1/activities")
@RequiredArgsConstructor
public class ActivityController {
    private final ActivityService activityService;

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
} 