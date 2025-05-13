package com.salesflow.activity.service;

import com.salesflow.activity.client.ContactCoreClient;
import com.salesflow.activity.dto.ActivityDTO;
import com.salesflow.activity.dto.ApiResponse;
import com.salesflow.activity.dto.TimelineDTO;
import com.salesflow.activity.model.Activity;
import com.salesflow.activity.model.ActivityType;
import com.salesflow.activity.repository.ActivityRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ActivityService {
    private final ActivityRepository activityRepository;
    private final ContactCoreClient contactCoreClient;

    @Transactional
    public ApiResponse<ActivityDTO> createActivity(ActivityDTO activityDTO) {
        log.debug("Creating activity for contact ID: {}", activityDTO.getContactId());
        try {
            // TODO: Re-enable contact existence check once contact-core-service is available
            /*
            // Verify if contact exists
            ResponseEntity<Boolean> response = contactCoreClient.checkContactExists(activityDTO.getContactId());
            log.debug("Contact existence check response: {}", response);
            
            if (!Boolean.TRUE.equals(response.getBody())) {
                log.error("Contact not found with ID: {}", activityDTO.getContactId());
                return ApiResponse.error("Contact not found with ID: " + activityDTO.getContactId());
            }
            */

            Activity activity = new Activity();
            activity.setContactId(activityDTO.getContactId());
            activity.setType(activityDTO.getType());
            activity.setTitle(activityDTO.getTitle());
            activity.setDescription(activityDTO.getDescription());
            activity.setStatus(activityDTO.getStatus() != null ? activityDTO.getStatus() : "PENDING");
            activity.setPriority(activityDTO.getPriority());
            activity.setScheduledTime(activityDTO.getScheduledTime());
            activity.setAssignedTo(activityDTO.getAssignedTo());
            activity.setCreatedBy(activityDTO.getCreatedBy());
            activity.setUpdatedBy(activityDTO.getUpdatedBy());

            Activity savedActivity = activityRepository.save(activity);
            if (savedActivity == null) {
                log.error("Failed to save activity for contact ID: {}", activityDTO.getContactId());
                return ApiResponse.error("Failed to save activity");
            }
            
            log.debug("Successfully created activity with ID: {}", savedActivity.getId());
            return ApiResponse.success("Activity created successfully", mapToDTO(savedActivity));
        } catch (Exception e) {
            log.error("Error creating activity: ", e);
            return ApiResponse.error("Error creating activity: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public ApiResponse<ActivityDTO> getActivity(UUID id) {
        try {
            Activity activity = activityRepository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Activity not found with ID: " + id));
            return ApiResponse.success("Activity retrieved successfully", mapToDTO(activity));
        } catch (EntityNotFoundException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public ApiResponse<List<ActivityDTO>> getActivitiesByContact(UUID contactId) {
        try {
            List<ActivityDTO> activities = activityRepository.findByContactId(contactId).stream()
                    .map(this::mapToDTO)
                    .collect(Collectors.toList());
            return ApiResponse.success("Activities retrieved successfully", activities);
        } catch (Exception e) {
            return ApiResponse.error("Error retrieving activities: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public ApiResponse<List<ActivityDTO>> getActivitiesByType(UUID contactId, ActivityType type) {
        try {
            List<ActivityDTO> activities = activityRepository.findByContactIdAndType(contactId, type)
                    .stream()
                    .map(this::mapToDTO)
                    .collect(Collectors.toList());
            return ApiResponse.success("Activities retrieved successfully", activities);
        } catch (Exception e) {
            return ApiResponse.error("Error retrieving activities: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public ApiResponse<List<ActivityDTO>> getPendingActivities(String assignedTo) {
        try {
            List<ActivityDTO> activities = activityRepository.findByAssignedToAndCompletedTimeIsNull(assignedTo).stream()
                    .map(this::mapToDTO)
                    .collect(Collectors.toList());
            return ApiResponse.success("Pending activities retrieved successfully", activities);
        } catch (Exception e) {
            return ApiResponse.error("Error retrieving pending activities: " + e.getMessage());
        }
    }

    @Transactional
    public ApiResponse<ActivityDTO> updateActivity(UUID id, ActivityDTO activityDTO) {
        try {
            Activity activity = activityRepository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Activity not found with ID: " + id));

            activity.setType(activityDTO.getType());
            activity.setTitle(activityDTO.getTitle());
            activity.setDescription(activityDTO.getDescription());
            activity.setStatus(activityDTO.getStatus());
            activity.setPriority(activityDTO.getPriority());
            activity.setScheduledTime(activityDTO.getScheduledTime());
            activity.setAssignedTo(activityDTO.getAssignedTo());
            activity.setUpdatedBy(activityDTO.getUpdatedBy());

            Activity updatedActivity = activityRepository.save(activity);
            return ApiResponse.success("Activity updated successfully", mapToDTO(updatedActivity));
        } catch (EntityNotFoundException e) {
            return ApiResponse.error(e.getMessage());
        } catch (Exception e) {
            return ApiResponse.error("Error updating activity: " + e.getMessage());
        }
    }

    @Transactional
    public ApiResponse<Void> deleteActivity(UUID id) {
        try {
            if (!activityRepository.existsById(id)) {
                return ApiResponse.error("Activity not found with ID: " + id);
            }
            activityRepository.deleteById(id);
            return ApiResponse.success("Activity deleted successfully", null);
        } catch (Exception e) {
            return ApiResponse.error("Error deleting activity: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public ApiResponse<TimelineDTO> getContactTimeline(UUID contactId, LocalDateTime startDate) {
        try {
            List<Activity> activities = activityRepository.findByContactIdAndDateRange(contactId, startDate, LocalDateTime.now());
            TimelineDTO timelineDTO = new TimelineDTO();
            timelineDTO.setContactId(contactId);
            timelineDTO.setActivities(activities.stream()
                    .map(this::mapToDTO)
                    .collect(Collectors.toList()));
            return ApiResponse.success("Timeline retrieved successfully", timelineDTO);
        } catch (Exception e) {
            return ApiResponse.error("Error retrieving timeline: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public ApiResponse<List<ActivityDTO>> searchActivities(String query, ActivityType type, String assignedTo,
                                            LocalDateTime startDate, LocalDateTime endDate) {
        try {
            List<ActivityDTO> activities = activityRepository.searchActivities(query, type, assignedTo, startDate, endDate).stream()
                    .map(this::mapToDTO)
                    .collect(Collectors.toList());
            return ApiResponse.success("Activities searched successfully", activities);
        } catch (Exception e) {
            return ApiResponse.error("Error searching activities: " + e.getMessage());
        }
    }

    @Transactional
    public ApiResponse<ActivityDTO> completeActivity(UUID id) {
        try {
            Activity activity = activityRepository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Activity not found with ID: " + id));

            activity.setStatus("COMPLETED");
            activity.setCompletedTime(LocalDateTime.now());

            Activity completedActivity = activityRepository.save(activity);
            return ApiResponse.success("Activity completed successfully", mapToDTO(completedActivity));
        } catch (EntityNotFoundException e) {
            return ApiResponse.error(e.getMessage());
        } catch (Exception e) {
            return ApiResponse.error("Error completing activity: " + e.getMessage());
        }
    }

    private ActivityDTO mapToDTO(Activity activity) {
        ActivityDTO dto = new ActivityDTO();
        dto.setId(activity.getId());
        dto.setContactId(activity.getContactId());
        dto.setType(activity.getType());
        dto.setTitle(activity.getTitle());
        dto.setDescription(activity.getDescription());
        dto.setStatus(activity.getStatus());
        dto.setPriority(activity.getPriority());
        dto.setScheduledTime(activity.getScheduledTime());
        dto.setCompletedTime(activity.getCompletedTime());
        dto.setAssignedTo(activity.getAssignedTo());
        dto.setCreatedBy(activity.getCreatedBy());
        dto.setUpdatedBy(activity.getUpdatedBy());
        dto.setCreatedAt(activity.getCreatedAt());
        dto.setUpdatedAt(activity.getUpdatedAt());
        return dto;
    }
} 