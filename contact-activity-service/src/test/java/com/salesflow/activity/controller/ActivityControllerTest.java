package com.salesflow.activity.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.salesflow.activity.config.TestSecurityConfig;
import com.salesflow.activity.dto.ActivityDTO;
import com.salesflow.activity.dto.ActivityStatsDTO;
import com.salesflow.activity.dto.ApiResponse;
import com.salesflow.activity.dto.TimelineDTO;
import com.salesflow.activity.model.ActivityType;
import com.salesflow.activity.service.ActivityService;
import com.salesflow.activity.service.ActivityStatsService;
import com.salesflow.activity.client.ContactCoreClient;
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

    @MockBean
    private ContactCoreClient contactCoreClient;

    private UUID testId;
    private UUID testContactId;
    private ActivityDTO testActivityDTO;
    private ApiResponse<ActivityDTO> successResponse;
    private ApiResponse<List<ActivityDTO>> successListResponse;
    private ApiResponse<TimelineDTO> successTimelineResponse;

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
        
        successResponse = ApiResponse.success("Success", testActivityDTO);
        successListResponse = ApiResponse.success("Success", Arrays.asList(testActivityDTO));
        
        TimelineDTO timelineDTO = new TimelineDTO();
        timelineDTO.setContactId(testContactId);
        timelineDTO.setActivities(Arrays.asList(testActivityDTO));
        successTimelineResponse = ApiResponse.success("Success", timelineDTO);

        when(contactCoreClient.checkContactExists(any(UUID.class)))
            .thenReturn(org.springframework.http.ResponseEntity.ok(true));
    }

    @Test
    void createActivity_Success() throws Exception {
        when(activityService.createActivity(any(ActivityDTO.class))).thenReturn(successResponse);

        mockMvc.perform(post("/api/v1/activities")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testActivityDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Success"))
                .andExpect(jsonPath("$.data.id").value(testId.toString()));
    }

    @Test
    void getActivity_Success() throws Exception {
        when(activityService.getActivity(testId)).thenReturn(successResponse);

        mockMvc.perform(get("/api/v1/activities/{id}", testId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Success"))
                .andExpect(jsonPath("$.data.id").value(testId.toString()));
    }

    @Test
    void getActivitiesByContact_Success() throws Exception {
        when(activityService.getActivitiesByContact(testContactId)).thenReturn(successListResponse);

        mockMvc.perform(get("/api/v1/activities/contact/{contactId}", testContactId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Success"))
                .andExpect(jsonPath("$.data[0].id").value(testId.toString()));
    }

    @Test
    void getActivitiesByType_Success() throws Exception {
        when(activityService.getActivitiesByType(testContactId, ActivityType.CALL)).thenReturn(successListResponse);

        mockMvc.perform(get("/api/v1/activities/contact/{contactId}/type/{type}", testContactId, ActivityType.CALL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Success"))
                .andExpect(jsonPath("$.data[0].id").value(testId.toString()));
    }

    @Test
    void updateActivity_Success() throws Exception {
        when(activityService.updateActivity(any(UUID.class), any(ActivityDTO.class))).thenReturn(successResponse);

        mockMvc.perform(put("/api/v1/activities/{id}", testId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testActivityDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Success"))
                .andExpect(jsonPath("$.data.id").value(testId.toString()));
    }

    @Test
    void deleteActivity_Success() throws Exception {
        when(activityService.deleteActivity(testId)).thenReturn(ApiResponse.success("Success", null));

        mockMvc.perform(delete("/api/v1/activities/{id}", testId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Success"));
    }

    @Test
    void getContactTimeline_Success() throws Exception {
        when(activityService.getContactTimeline(any(UUID.class), any(LocalDateTime.class)))
                .thenReturn(successTimelineResponse);

        mockMvc.perform(get("/api/v1/activities/contact/{contactId}/timeline", testContactId)
                .param("startDate", LocalDateTime.now().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Success"))
                .andExpect(jsonPath("$.data.contactId").value(testContactId.toString()));
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