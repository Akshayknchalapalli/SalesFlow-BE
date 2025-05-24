package com.salesflow.auth.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.ActiveProfiles;

import com.salesflow.auth.domain.User;
import com.salesflow.auth.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
@SpringBootTest
@ActiveProfiles("test")
class CustomUserDetailsServiceTest {

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @MockBean
    private UserRepository userRepository;

    @Test
    void loadUserByUsername_WhenUserExists_ShouldReturnUserDetails() {
        User mockUser = User.builder()
                .username("testuser")
                .password("encoded-pw")
                .email("test@example.com")
                .build();
        
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(mockUser));

        UserDetails details = userDetailsService.loadUserByUsername("testuser");
        assertNotNull(details);
        assertEquals("testuser", details.getUsername());
    }

    @Test
    void loadUserByUsername_WhenUserMissing_ShouldThrow() {
        when(userRepository.findByUsername("missing")).thenReturn(Optional.empty());
        
        assertThrows(UsernameNotFoundException.class, () -> 
            userDetailsService.loadUserByUsername("missing")
        );
    }
}