package com.salesflow.auth.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.salesflow.auth.dto.AuthResponse;
import com.salesflow.auth.filter.JwtValidationFilter;
import com.salesflow.auth.filter.TenantValidationFilter;
import com.salesflow.auth.repository.RoleRepository;
import com.salesflow.auth.repository.TokenRepository;
import com.salesflow.auth.repository.UserRepository;
import com.salesflow.auth.service.CustomUserDetails;
import com.salesflow.auth.service.CustomUserDetailsService;
import com.salesflow.auth.service.JwtAuthenticationEntryPoint;
import com.salesflow.auth.service.JwtAuthenticationFilter;
import com.salesflow.auth.service.JwtService;
import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import com.salesflow.auth.domain.User;
import com.salesflow.auth.domain.Role;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;


@Configuration
@Profile("test")
@EnableMethodSecurity
@EnableWebSecurity
public class TestSecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    @Primary
    public UserRepository userRepository() {
        User mockUser = User.builder()
                .username("testuser")
                .password(passwordEncoder().encode("password"))
                .email("test@example.com")
                .tenantId("test-tenant")
                .enabled(true)
                .build();
        
        UserRepository mockRepo = Mockito.mock(UserRepository.class);
        Mockito.when(mockRepo.findByUsername("testuser")).thenReturn(Optional.of(mockUser));
        return mockRepo;
    }
    
    @Bean
    @Primary
    public RoleRepository roleRepository() {
        Role userRole = new Role(1L, "ROLE_USER");
        Role adminRole = new Role(2L, "ROLE_ADMIN");
        Role tenantAdminRole = new Role(3L, "ROLE_TENANT_ADMIN");
        
        RoleRepository mockRepo = Mockito.mock(RoleRepository.class);
        Mockito.when(mockRepo.findByName("ROLE_USER")).thenReturn(Optional.of(userRole));
        Mockito.when(mockRepo.findByName("ROLE_ADMIN")).thenReturn(Optional.of(adminRole));
        Mockito.when(mockRepo.findByName("ROLE_TENANT_ADMIN")).thenReturn(Optional.of(tenantAdminRole));
        return mockRepo;
    }
    
    @Bean
    @Primary
    public TokenRepository tokenRepository() {
        return Mockito.mock(TokenRepository.class);
    }
    
    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
        InMemoryUserDetailsManager manager = new InMemoryUserDetailsManager();
        manager.createUser(org.springframework.security.core.userdetails.User.withUsername("testuser")
            .password(passwordEncoder.encode("password"))
            .roles("USER")
            .build());
        manager.createUser(org.springframework.security.core.userdetails.User.withUsername("admin")
            .password(passwordEncoder.encode("password"))
            .roles("ADMIN")
            .build());
        return manager;
    }
    
    @Bean
    @Primary
    public CustomUserDetailsService customUserDetailsService() {
        return Mockito.mock(CustomUserDetailsService.class);
    }
    
    @Bean
    @Primary
    public JwtService jwtService() {
        JwtService mockService = Mockito.mock(JwtService.class);
        Mockito.when(mockService.validateToken(Mockito.anyString(), Mockito.any()))
               .thenReturn(true);
        return mockService;
    }
    
    @Bean
    @Primary
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return Mockito.mock(JwtAuthenticationFilter.class);
    }
    
    @Bean
    @Primary
    public JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint() {
        return Mockito.mock(JwtAuthenticationEntryPoint.class);
    }
    
    @Bean
    @Primary
    public TenantValidationFilter tenantValidationFilter() {
        return Mockito.mock(TenantValidationFilter.class);
    }
    
    @Bean
    @Primary
    public AuthenticationManager authenticationManager() throws Exception {
        return Mockito.mock(AuthenticationManager.class);
    }
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthFilter, JwtAuthenticationEntryPoint entryPoint) throws Exception {
        http.csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(new AntPathRequestMatcher("/api/auth/**")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/api/test/public")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/api/test/admin")).hasRole("ADMIN")
                .requestMatchers(new AntPathRequestMatcher("/api/test/user")).hasRole("USER")
                .anyRequest().authenticated())
            .exceptionHandling(except -> except.authenticationEntryPoint(entryPoint))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .addFilterBefore(jwtAuthFilter, org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
    
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}