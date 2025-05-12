package com.salesflow.activity.service;

import com.salesflow.activity.dto.ActivityDTO;
import com.salesflow.activity.dto.TimelineDTO;
import com.salesflow.activity.model.Activity;
import com.salesflow.activity.model.ActivityType;
import com.salesflow.activity.repository.ActivityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ActivityService {
    private final ActivityRepository activityRepository;

    @Transactional
    public ActivityDTO createActivity(ActivityDTO activityDTO) {
        Activity activity = convertToEntity(activityDTO);
        Activity savedActivity = activityRepository.save(activity);
        return convertToDTO(savedActivity);
    }

    @Transactional(readOnly = true)
    public ActivityDTO getActivity(Long id) {
        Activity activity = activityRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Activity not found with id: " + id));
        return convertToDTO(activity);
    }

    @Transactional(readOnly = true)
    public List<ActivityDTO> getActivitiesByContact(Long contactId) {
        return activityRepository.findByContactId(contactId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ActivityDTO> getActivitiesByType(Long contactId, ActivityType type) {
        return activityRepository.findByContactIdAndType(contactId, type)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ActivityDTO> getPendingActivities(String assignedTo) {
        return activityRepository.findByAssignedToAndCompletedAtIsNull(assignedTo)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public ActivityDTO completeActivity(Long id) {
        Activity activity = activityRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Activity not found with id: " + id));
        activity.setCompletedAt(LocalDateTime.now());
        Activity updatedActivity = activityRepository.save(activity);
        return convertToDTO(updatedActivity);
    }

    @Transactional
    public ActivityDTO updateActivity(Long id, ActivityDTO activityDTO) {
        Activity existingActivity = activityRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Activity not found with id: " + id));
        
        updateEntityFromDTO(existingActivity, activityDTO);
        Activity updatedActivity = activityRepository.save(existingActivity);
        return convertToDTO(updatedActivity);
    }

    @Transactional(readOnly = true)
    public List<TimelineDTO> getContactTimeline(Long contactId, LocalDateTime startDate, LocalDateTime endDate) {
        List<Activity> activities = activityRepository.findByContactId(contactId);
        
        return activities.stream()
                .filter(activity -> {
                    if (startDate != null && activity.getScheduledAt().isBefore(startDate)) {
                        return false;
                    }
                    if (endDate != null && activity.getScheduledAt().isAfter(endDate)) {
                        return false;
                    }
                    return true;
                })
                .map(this::convertToTimelineDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ActivityDTO> searchActivities(String query, ActivityType type, String assignedTo, 
                                            LocalDateTime startDate, LocalDateTime endDate) {
        List<Activity> activities = activityRepository.findAll();
        
        return activities.stream()
                .filter(activity -> {
                    if (query != null && !query.isEmpty()) {
                        if (!activity.getNotes().toLowerCase().contains(query.toLowerCase()) &&
                            !activity.getOutcome().toLowerCase().contains(query.toLowerCase())) {
                            return false;
                        }
                    }
                    if (type != null && activity.getType() != type) {
                        return false;
                    }
                    if (assignedTo != null && !assignedTo.equals(activity.getAssignedTo())) {
                        return false;
                    }
                    if (startDate != null && activity.getScheduledAt().isBefore(startDate)) {
                        return false;
                    }
                    if (endDate != null && activity.getScheduledAt().isAfter(endDate)) {
                        return false;
                    }
                    return true;
                })
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private Activity convertToEntity(ActivityDTO dto) {
        Activity activity = new Activity();
        activity.setContactId(dto.getContactId());
        activity.setType(dto.getType());
        activity.setOutcome(dto.getOutcome());
        activity.setNotes(dto.getNotes());
        activity.setScheduledAt(dto.getScheduledAt());
        activity.setCompletedAt(dto.getCompletedAt());
        activity.setAssignedTo(dto.getAssignedTo());
        activity.setNextStep(dto.getNextStep());
        return activity;
    }

    private ActivityDTO convertToDTO(Activity activity) {
        ActivityDTO dto = new ActivityDTO();
        dto.setId(activity.getId());
        dto.setContactId(activity.getContactId());
        dto.setType(activity.getType());
        dto.setOutcome(activity.getOutcome());
        dto.setNotes(activity.getNotes());
        dto.setScheduledAt(activity.getScheduledAt());
        dto.setCompletedAt(activity.getCompletedAt());
        dto.setAssignedTo(activity.getAssignedTo());
        dto.setNextStep(activity.getNextStep());
        dto.setCreatedAt(activity.getCreatedAt());
        dto.setUpdatedAt(activity.getUpdatedAt());
        return dto;
    }

    private TimelineDTO convertToTimelineDTO(Activity activity) {
        TimelineDTO dto = new TimelineDTO();
        dto.setId(activity.getId());
        dto.setContactId(activity.getContactId());
        dto.setType(activity.getType());
        dto.setOutcome(activity.getOutcome());
        dto.setNotes(activity.getNotes());
        dto.setTimestamp(activity.getScheduledAt());
        dto.setAssignedTo(activity.getAssignedTo());
        dto.setNextStep(activity.getNextStep());
        dto.setCompleted(activity.getCompletedAt() != null);
        dto.setCompletedAt(activity.getCompletedAt());
        return dto;
    }

    private void updateEntityFromDTO(Activity activity, ActivityDTO dto) {
        if (dto.getType() != null) activity.setType(dto.getType());
        if (dto.getOutcome() != null) activity.setOutcome(dto.getOutcome());
        if (dto.getNotes() != null) activity.setNotes(dto.getNotes());
        if (dto.getScheduledAt() != null) activity.setScheduledAt(dto.getScheduledAt());
        if (dto.getCompletedAt() != null) activity.setCompletedAt(dto.getCompletedAt());
        if (dto.getAssignedTo() != null) activity.setAssignedTo(dto.getAssignedTo());
        if (dto.getNextStep() != null) activity.setNextStep(dto.getNextStep());
    }
} 