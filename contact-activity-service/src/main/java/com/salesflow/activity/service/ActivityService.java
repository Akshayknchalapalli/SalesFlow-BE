package com.salesflow.activity.service;

import com.salesflow.activity.client.ContactCoreClient;
import com.salesflow.activity.dto.ActivityDTO;
import com.salesflow.activity.dto.TimelineDTO;
import com.salesflow.activity.model.Activity;
import com.salesflow.activity.model.ActivityType;
import com.salesflow.activity.repository.ActivityRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ActivityService {
    private final ActivityRepository activityRepository;
    private final ContactCoreClient contactCoreClient;

    @Transactional
    public ActivityDTO createActivity(ActivityDTO activityDTO) {
        // Verify if contact exists
        ResponseEntity<Boolean> response = contactCoreClient.checkContactExists(activityDTO.getContactId());
        if (!Boolean.TRUE.equals(response.getBody())) {
            throw new EntityNotFoundException("Contact not found with ID: " + activityDTO.getContactId());
        }

        Activity activity = new Activity();
        activity.setContactId(activityDTO.getContactId());
        activity.setType(activityDTO.getType());
        activity.setTitle(activityDTO.getTitle());
        activity.setDescription(activityDTO.getDescription());
        activity.setStatus(activityDTO.getStatus());
        activity.setPriority(activityDTO.getPriority());
        activity.setScheduledTime(activityDTO.getScheduledTime());
        activity.setAssignedTo(activityDTO.getAssignedTo());
        activity.setCreatedBy(activityDTO.getCreatedBy());
        activity.setUpdatedBy(activityDTO.getUpdatedBy());

        Activity savedActivity = activityRepository.save(activity);
        return mapToDTO(savedActivity);
    }

    @Transactional(readOnly = true)
    public ActivityDTO getActivity(UUID id) {
        Activity activity = activityRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Activity not found with ID: " + id));
        return mapToDTO(activity);
    }

    @Transactional(readOnly = true)
    public List<ActivityDTO> getActivitiesByContact(UUID contactId) {
        return activityRepository.findByContactId(contactId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ActivityDTO> getActivitiesByType(UUID contactId, ActivityType type) {
        return activityRepository.findByContactIdAndType(contactId, type)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ActivityDTO> getPendingActivities(String assignedTo) {
        return activityRepository.findByAssignedToAndCompletedTimeIsNull(assignedTo).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public ActivityDTO updateActivity(UUID id, ActivityDTO activityDTO) {
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
        return mapToDTO(updatedActivity);
    }

    @Transactional
    public void deleteActivity(UUID id) {
        if (!activityRepository.existsById(id)) {
            throw new EntityNotFoundException("Activity not found with ID: " + id);
        }
        activityRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public TimelineDTO getContactTimeline(UUID contactId, LocalDateTime startDate) {
        List<Activity> activities = activityRepository.findByContactIdAndDateRange(contactId, startDate, LocalDateTime.now());
        TimelineDTO timelineDTO = new TimelineDTO();
        timelineDTO.setContactId(contactId);
        timelineDTO.setActivities(activities.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList()));
        return timelineDTO;
    }

    @Transactional(readOnly = true)
    public List<ActivityDTO> searchActivities(String query, ActivityType type, String assignedTo,
                                            LocalDateTime startDate, LocalDateTime endDate) {
        return activityRepository.searchActivities(query, type, assignedTo, startDate, endDate).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public ActivityDTO completeActivity(UUID id) {
        Activity activity = activityRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Activity not found with ID: " + id));

        activity.setStatus("COMPLETED");
        activity.setCompletedTime(LocalDateTime.now());

        Activity completedActivity = activityRepository.save(activity);
        return mapToDTO(completedActivity);
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