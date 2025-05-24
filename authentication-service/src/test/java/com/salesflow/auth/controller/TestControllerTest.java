package com.salesflow.auth.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
public class TestControllerTest {

    @InjectMocks
    private TestController testController;

    @Test
    public void publicEndpoint_ShouldReturnCorrectMessage() {
        ResponseEntity<String> response = testController.publicEndpoint();
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Public endpoint accessible to all", response.getBody());
    }

    @Test
    public void userEndpoint_ShouldReturnCorrectMessage() {
        ResponseEntity<String> response = testController.userEndpoint();
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("User endpoint accessible to authenticated users", response.getBody());
    }

    @Test
    public void adminEndpoint_ShouldReturnCorrectMessage() {
        ResponseEntity<String> response = testController.adminEndpoint();
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Admin endpoint accessible to admins only", response.getBody());
    }
} 