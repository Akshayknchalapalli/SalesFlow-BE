package com.salesflow.activity.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.salesflow.activity.dto.ActivityDTO;
import com.salesflow.activity.model.ActivityType;
import com.salesflow.activity.service.ActivityService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ActivityController.class)
class ActivityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ActivityService activityService;

    @Test
    void createActivity_Success() throws Exception {
        ActivityDTO activityDTO = new ActivityDTO();
        activityDTO.setContactId(100L);
        activityDTO.setType(ActivityType.CALL);
        activityDTO.setOutcome("Demo Scheduled");
        activityDTO.setNotes("Test notes");
        activityDTO.setScheduledAt(LocalDateTime.now());
        activityDTO.setAssignedTo("Sarah");

        when(activityService.createActivity(any(ActivityDTO.class))).thenReturn(activityDTO);

        mockMvc.perform(post("/api/v1/activities")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(activityDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contactId").value(100L))
                .andExpect(jsonPath("$.type").value("CALL"))
                .andExpect(jsonPath("$.outcome").value("Demo Scheduled"));
    }

    @Test
    void getActivity_Success() throws Exception {
        ActivityDTO activityDTO = new ActivityDTO();
        activityDTO.setId(1L);
        activityDTO.setContactId(100L);
        activityDTO.setType(ActivityType.CALL);

        when(activityService.getActivity(1L)).thenReturn(activityDTO);

        mockMvc.perform(get("/api/v1/activities/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.contactId").value(100L));
    }

    @Test
    void getActivitiesByContact_Success() throws Exception {
        ActivityDTO activityDTO = new ActivityDTO();
        activityDTO.setId(1L);
        activityDTO.setContactId(100L);
        activityDTO.setType(ActivityType.CALL);

        when(activityService.getActivitiesByContact(100L)).thenReturn(Arrays.asList(activityDTO));

        mockMvc.perform(get("/api/v1/activities/contact/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].contactId").value(100L));
    }

    @Test
    void completeActivity_Success() throws Exception {
        ActivityDTO activityDTO = new ActivityDTO();
        activityDTO.setId(1L);
        activityDTO.setContactId(100L);
        activityDTO.setType(ActivityType.CALL);
        activityDTO.setCompletedAt(LocalDateTime.now());

        when(activityService.completeActivity(1L)).thenReturn(activityDTO);

        mockMvc.perform(put("/api/v1/activities/1/complete"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.completedAt").exists());
    }
} 