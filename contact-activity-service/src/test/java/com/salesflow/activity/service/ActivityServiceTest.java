package com.salesflow.activity.service;

import com.salesflow.activity.dto.ActivityDTO;
import com.salesflow.activity.model.Activity;
import com.salesflow.activity.model.ActivityType;
import com.salesflow.activity.repository.ActivityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ActivityServiceTest {

    @Mock
    private ActivityRepository activityRepository;

    @InjectMocks
    private ActivityService activityService;

    private Activity activity;
    private ActivityDTO activityDTO;

    @BeforeEach
    void setUp() {
        activity = new Activity();
        activity.setId(1L);
        activity.setContactId(100L);
        activity.setType(ActivityType.CALL);
        activity.setOutcome("Demo Scheduled");
        activity.setNotes("Test notes");
        activity.setScheduledAt(LocalDateTime.now());
        activity.setAssignedTo("Sarah");

        activityDTO = new ActivityDTO();
        activityDTO.setContactId(100L);
        activityDTO.setType(ActivityType.CALL);
        activityDTO.setOutcome("Demo Scheduled");
        activityDTO.setNotes("Test notes");
        activityDTO.setScheduledAt(LocalDateTime.now());
        activityDTO.setAssignedTo("Sarah");
    }

    @Test
    void createActivity_Success() {
        when(activityRepository.save(any(Activity.class))).thenReturn(activity);

        ActivityDTO result = activityService.createActivity(activityDTO);

        assertNotNull(result);
        assertEquals(activityDTO.getContactId(), result.getContactId());
        assertEquals(activityDTO.getType(), result.getType());
        verify(activityRepository, times(1)).save(any(Activity.class));
    }

    @Test
    void getActivity_Success() {
        when(activityRepository.findById(1L)).thenReturn(Optional.of(activity));

        ActivityDTO result = activityService.getActivity(1L);

        assertNotNull(result);
        assertEquals(activity.getId(), result.getId());
        assertEquals(activity.getContactId(), result.getContactId());
    }

    @Test
    void getActivity_NotFound() {
        when(activityRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> activityService.getActivity(1L));
    }

    @Test
    void getActivitiesByContact_Success() {
        when(activityRepository.findByContactId(100L)).thenReturn(Arrays.asList(activity));

        List<ActivityDTO> results = activityService.getActivitiesByContact(100L);

        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals(activity.getContactId(), results.get(0).getContactId());
    }

    @Test
    void completeActivity_Success() {
        when(activityRepository.findById(1L)).thenReturn(Optional.of(activity));
        when(activityRepository.save(any(Activity.class))).thenReturn(activity);

        ActivityDTO result = activityService.completeActivity(1L);

        assertNotNull(result);
        assertNotNull(result.getCompletedAt());
        verify(activityRepository, times(1)).save(any(Activity.class));
    }
} 