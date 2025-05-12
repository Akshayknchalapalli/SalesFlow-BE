package com.salesflow.activity.service;

import com.salesflow.activity.client.ContactCoreClient;
import com.salesflow.activity.dto.ActivityDTO;
import com.salesflow.activity.dto.TimelineDTO;
import com.salesflow.activity.model.Activity;
import com.salesflow.activity.model.ActivityType;
import com.salesflow.activity.repository.ActivityRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ActivityServiceTest {

    @Mock
    private ActivityRepository activityRepository;

    @Mock
    private ContactCoreClient contactCoreClient;

    @InjectMocks
    private ActivityService activityService;

    private UUID testId;
    private UUID testContactId;
    private Activity testActivity;
    private ActivityDTO testActivityDTO;

    @BeforeEach
    void setUp() {
        testId = UUID.randomUUID();
        testContactId = UUID.randomUUID();
        
        testActivity = new Activity();
        testActivity.setId(testId);
        testActivity.setContactId(testContactId);
        testActivity.setType(ActivityType.CALL);
        testActivity.setTitle("Test Activity");
        testActivity.setDescription("Test Description");
        testActivity.setScheduledTime(LocalDateTime.now().plusDays(1));
        
        testActivityDTO = new ActivityDTO();
        testActivityDTO.setId(testId);
        testActivityDTO.setContactId(testContactId);
        testActivityDTO.setType(ActivityType.CALL);
        testActivityDTO.setTitle("Test Activity");
        testActivityDTO.setDescription("Test Description");
        testActivityDTO.setScheduledTime(LocalDateTime.now().plusDays(1));
    }

    @Test
    void createActivity_Success() {
        when(contactCoreClient.checkContactExists(testContactId)).thenReturn(ResponseEntity.ok(true));
        when(activityRepository.save(any(Activity.class))).thenReturn(testActivity);

        ActivityDTO result = activityService.createActivity(testActivityDTO);

        assertNotNull(result);
        assertEquals(testId, result.getId());
        assertEquals(testContactId, result.getContactId());
        assertEquals(ActivityType.CALL, result.getType());
        assertEquals("Test Activity", result.getTitle());
    }

    @Test
    void createActivity_ContactNotFound() {
        when(contactCoreClient.checkContactExists(testContactId)).thenReturn(ResponseEntity.ok(false));

        assertThrows(EntityNotFoundException.class, () -> activityService.createActivity(testActivityDTO));
    }

    @Test
    void getActivity_Success() {
        when(activityRepository.findById(testId)).thenReturn(Optional.of(testActivity));

        ActivityDTO result = activityService.getActivity(testId);

        assertNotNull(result);
        assertEquals(testId, result.getId());
        assertEquals(testContactId, result.getContactId());
    }

    @Test
    void getActivity_NotFound() {
        when(activityRepository.findById(testId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> activityService.getActivity(testId));
    }

    @Test
    void getActivitiesByContact_Success() {
        when(activityRepository.findByContactId(testContactId)).thenReturn(Arrays.asList(testActivity));

        List<ActivityDTO> results = activityService.getActivitiesByContact(testContactId);

        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals(testId, results.get(0).getId());
        assertEquals(testContactId, results.get(0).getContactId());
    }

    @Test
    void updateActivity_Success() {
        when(activityRepository.findById(testId)).thenReturn(Optional.of(testActivity));
        when(activityRepository.save(any(Activity.class))).thenReturn(testActivity);

        ActivityDTO result = activityService.updateActivity(testId, testActivityDTO);

        assertNotNull(result);
        assertEquals(testId, result.getId());
        assertEquals(testContactId, result.getContactId());
    }

    @Test
    void updateActivity_NotFound() {
        when(activityRepository.findById(testId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> activityService.updateActivity(testId, testActivityDTO));
    }

    @Test
    void completeActivity_Success() {
        when(activityRepository.findById(testId)).thenReturn(Optional.of(testActivity));
        when(activityRepository.save(any(Activity.class))).thenReturn(testActivity);

        ActivityDTO result = activityService.completeActivity(testId);

        assertNotNull(result);
        assertEquals(testId, result.getId());
        assertEquals(testContactId, result.getContactId());
        assertNotNull(result.getCompletedTime());
    }

    @Test
    void completeActivity_NotFound() {
        when(activityRepository.findById(testId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> activityService.completeActivity(testId));
    }

    @Test
    void deleteActivity_Success() {
        when(activityRepository.existsById(testId)).thenReturn(true);
        doNothing().when(activityRepository).deleteById(testId);

        assertDoesNotThrow(() -> activityService.deleteActivity(testId));
        verify(activityRepository).deleteById(testId);
    }

    @Test
    void deleteActivity_NotFound() {
        when(activityRepository.existsById(testId)).thenReturn(false);

        assertThrows(EntityNotFoundException.class, () -> activityService.deleteActivity(testId));
    }

    @Test
    void getContactTimeline_Success() {
        when(activityRepository.findByContactIdAndDateRange(eq(testContactId), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Arrays.asList(testActivity));

        TimelineDTO result = activityService.getContactTimeline(testContactId, LocalDateTime.now().minusDays(7));

        assertNotNull(result);
        assertEquals(testContactId, result.getContactId());
        assertEquals(1, result.getActivities().size());
        assertEquals(testId, result.getActivities().get(0).getId());
    }
} 