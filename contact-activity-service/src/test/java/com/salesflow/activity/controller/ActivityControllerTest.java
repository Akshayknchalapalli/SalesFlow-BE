package com.salesflow.activity.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.salesflow.activity.dto.ActivityDTO;
import com.salesflow.activity.dto.ActivityStatsDTO;
import com.salesflow.activity.dto.TimelineDTO;
import com.salesflow.activity.model.ActivityType;
import com.salesflow.activity.service.ActivityService;
import com.salesflow.activity.service.ActivityStatsService;
import com.salesflow.activity.config.TestSecurityConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ActivityController.class)
@Import(TestSecurityConfig.class)
public class ActivityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ActivityService activityService;

    @MockBean
    private ActivityStatsService activityStatsService;

    private UUID testId;
    private UUID testContactId;
    private ActivityDTO testActivityDTO;

    @BeforeEach
    void setUp() {
        testId = UUID.randomUUID();
        testContactId = UUID.randomUUID();
        testActivityDTO = new ActivityDTO();
        testActivityDTO.setId(testId);
        testActivityDTO.setContactId(testContactId);
        testActivityDTO.setType(ActivityType.CALL);
        testActivityDTO.setTitle("Test Activity");
        testActivityDTO.setDescription("Test Description");
        testActivityDTO.setScheduledTime(LocalDateTime.now().plusDays(1));
    }

    @Test
    void testCreateActivity() throws Exception {
        when(activityService.createActivity(any(ActivityDTO.class))).thenReturn(testActivityDTO);

        mockMvc.perform(post("/api/v1/activities")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testActivityDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(testId.toString()))
                .andExpect(jsonPath("$.contactId").value(testContactId.toString()));
    }

    @Test
    void testGetActivity() throws Exception {
        when(activityService.getActivity(testId)).thenReturn(testActivityDTO);

        mockMvc.perform(get("/api/v1/activities/{id}", testId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testId.toString()))
                .andExpect(jsonPath("$.contactId").value(testContactId.toString()));
    }

    @Test
    void testGetActivitiesByContact() throws Exception {
        List<ActivityDTO> activities = Arrays.asList(testActivityDTO);
        when(activityService.getActivitiesByContact(testContactId)).thenReturn(activities);

        mockMvc.perform(get("/api/v1/activities/contact/{contactId}", testContactId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(testId.toString()))
                .andExpect(jsonPath("$[0].contactId").value(testContactId.toString()));
    }

    @Test
    void testUpdateActivity() throws Exception {
        when(activityService.updateActivity(eq(testId), any(ActivityDTO.class))).thenReturn(testActivityDTO);

        mockMvc.perform(put("/api/v1/activities/{id}", testId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testActivityDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testId.toString()))
                .andExpect(jsonPath("$.contactId").value(testContactId.toString()));
    }

    @Test
    void testCompleteActivity() throws Exception {
        when(activityService.completeActivity(testId)).thenReturn(testActivityDTO);

        mockMvc.perform(post("/api/v1/activities/{id}/complete", testId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testId.toString()))
                .andExpect(jsonPath("$.contactId").value(testContactId.toString()));
    }

    @Test
    void testDeleteActivity() throws Exception {
        mockMvc.perform(delete("/api/v1/activities/{id}", testId))
                .andExpect(status().isNoContent());
    }

    @Test
    void testGetContactTimeline() throws Exception {
        TimelineDTO timelineDTO = new TimelineDTO();
        timelineDTO.setContactId(testContactId);
        timelineDTO.setActivities(Arrays.asList(testActivityDTO));

        when(activityService.getContactTimeline(eq(testContactId), any(LocalDateTime.class)))
                .thenReturn(timelineDTO);

        mockMvc.perform(get("/api/v1/activities/contact/{contactId}/timeline", testContactId)
                .param("startDate", LocalDateTime.now().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contactId").value(testContactId.toString()))
                .andExpect(jsonPath("$.activities[0].id").value(testId.toString()));
    }

    @Test
    void testGetActivityStats() throws Exception {
        ActivityStatsDTO statsDTO = new ActivityStatsDTO();
        statsDTO.setContactId(testContactId);
        statsDTO.setTotalActivities(5);
        statsDTO.setCompletedActivities(3);

        when(activityStatsService.getActivityStats(testContactId)).thenReturn(statsDTO);

        mockMvc.perform(get("/api/v1/activities/contact/{contactId}/stats", testContactId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contactId").value(testContactId.toString()))
                .andExpect(jsonPath("$.totalActivities").value(5))
                .andExpect(jsonPath("$.completedActivities").value(3));
    }
} 