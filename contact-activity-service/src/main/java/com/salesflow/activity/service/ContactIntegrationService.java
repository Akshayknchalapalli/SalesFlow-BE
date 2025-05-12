package com.salesflow.activity.service;

import com.salesflow.activity.client.ContactCoreClient;
import com.salesflow.activity.dto.ActivityDTO;
import com.salesflow.activity.dto.ActivityStatsDTO;
import com.salesflow.activity.dto.TimelineDTO;
import com.salesflow.activity.model.Activity;
import com.salesflow.activity.model.ActivityType;
import com.salesflow.contact.core.domain.Contact;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ContactIntegrationService {
    private final ActivityService activityService;
    private final ActivityStatsService activityStatsService;
    private final ActivityNotificationService notificationService;
    private final ContactCoreClient contactCoreClient;

    @Transactional
    public ActivityDTO createContactActivity(Long contactId, ActivityDTO activityDTO) {
        // Validate contact exists through Contact Core Service
        if (!contactCoreClient.validateContact(contactId)) {
            throw new IllegalArgumentException("Contact not found with id: " + contactId);
        }
        
        ActivityDTO createdActivity = activityService.createActivity(activityDTO);
        
        // Send notification for new activity
        notificationService.sendReminder(activityService.getActivity(createdActivity.getId()));
        
        return createdActivity;
    }

    @Transactional(readOnly = true)
    public List<TimelineDTO> getContactTimeline(Long contactId, LocalDateTime startDate, LocalDateTime endDate) {
        // Validate contact exists
        if (!contactCoreClient.validateContact(contactId)) {
            throw new IllegalArgumentException("Contact not found with id: " + contactId);
        }
        
        // Get contact activities and convert to timeline format
        return activityService.getContactTimeline(contactId, startDate, endDate);
    }

    @Transactional(readOnly = true)
    public ActivityStatsDTO getContactActivityStats(Long contactId) {
        // Validate contact exists
        if (!contactCoreClient.validateContact(contactId)) {
            throw new IllegalArgumentException("Contact not found with id: " + contactId);
        }
        
        // Get comprehensive activity statistics for a contact
        return activityStatsService.getContactActivityStats(contactId);
    }

    @Transactional(readOnly = true)
    public List<ActivityDTO> getUpcomingActivities(Long contactId) {
        // Validate contact exists
        if (!contactCoreClient.validateContact(contactId)) {
            throw new IllegalArgumentException("Contact not found with id: " + contactId);
        }
        
        // Get all upcoming activities for a contact
        return activityService.getActivitiesByContact(contactId).stream()
                .filter(activity -> activity.getScheduledAt().isAfter(LocalDateTime.now()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ActivityDTO> getOverdueActivities(Long contactId) {
        // Validate contact exists
        if (!contactCoreClient.validateContact(contactId)) {
            throw new IllegalArgumentException("Contact not found with id: " + contactId);
        }
        
        // Get all overdue activities for a contact
        return activityService.getActivitiesByContact(contactId).stream()
                .filter(activity -> 
                    activity.getScheduledAt().isBefore(LocalDateTime.now()) && 
                    activity.getCompletedAt() == null)
                .toList();
    }

    @Transactional
    public ActivityDTO completeActivity(Long contactId, Long activityId) {
        // Validate contact exists
        if (!contactCoreClient.validateContact(contactId)) {
            throw new IllegalArgumentException("Contact not found with id: " + contactId);
        }
        
        // Complete the activity
        ActivityDTO activity = activityService.completeActivity(activityId);
        
        // Check if all required activities are completed
        ActivityStatsDTO stats = activityStatsService.getContactActivityStats(contactId);
        if (stats.getCompletionRate() == 100.0) {
            // Update contact stage to next stage
            contactCoreClient.updateContactStage(contactId, Contact.ContactStage.CUSTOMER.name());
        }
        
        return activity;
    }

    @Transactional(readOnly = true)
    public List<ActivityDTO> searchContactActivities(Long contactId, String query, ActivityType type, 
                                                   String assignedTo, LocalDateTime startDate, 
                                                   LocalDateTime endDate) {
        // Validate contact exists
        if (!contactCoreClient.validateContact(contactId)) {
            throw new IllegalArgumentException("Contact not found with id: " + contactId);
        }
        
        // Search activities for a specific contact with various filters
        return activityService.searchActivities(query, type, assignedTo, startDate, endDate).stream()
                .filter(activity -> activity.getContactId().equals(contactId))
                .toList();
    }
} 