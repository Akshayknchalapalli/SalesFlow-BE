package com.salesflow.auth.controller;

import com.salesflow.auth.config.TestSecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@WebMvcTest(controllers = TestController.class)
@Import(TestSecurityConfig.class)
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "spring.main.allow-bean-definition-overriding=true"
})
public class TestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(roles = "USER")
    public void publicEndpoint_ShouldBeAccessibleToAll() throws Exception {
        mockMvc.perform(get("/api/test/public"))
               .andExpect(status().isOk())
               .andExpect(content().string("Public endpoint accessible to all"));
    }

    @Test
    @WithMockUser(roles = "USER")
    public void userEndpoint_ShouldBeAccessibleToAuthenticatedUsers() throws Exception {
        mockMvc.perform(get("/api/test/user"))
               .andExpect(status().isOk())
               .andExpect(content().string("User endpoint accessible to authenticated users"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void adminEndpoint_ShouldBeAccessibleToAdmins() throws Exception {
        mockMvc.perform(get("/api/test/admin"))
               .andExpect(status().isOk())
               .andExpect(content().string("Admin endpoint accessible to admins only"));
    }

    @Test
    @WithMockUser(roles = "USER")
    public void adminEndpoint_ShouldNotBeAccessibleToUsers() throws Exception {
        mockMvc.perform(get("/api/test/admin"))
               .andExpect(status().isForbidden());
    }
} 