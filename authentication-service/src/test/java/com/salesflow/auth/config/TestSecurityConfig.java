package com.salesflow.auth.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.salesflow.auth.dto.AuthResponse;
import com.salesflow.auth.filter.JwtValidationFilter;
import com.salesflow.auth.repository.TokenRepository;
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

import java.util.Collections;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@Profile("test")
public class TestSecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
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
    public AuthenticationProvider authenticationProvider(UserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }
    
    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http, AuthenticationProvider authenticationProvider) throws Exception {
        return http.getSharedObject(AuthenticationManagerBuilder.class)
            .authenticationProvider(authenticationProvider)
            .build();
    }
    
    @Bean
    public JwtProperties jwtProperties() {
        JwtProperties props = new JwtProperties();
        props.setSecretKey("test-secret-key-for-unit-tests-not-for-production-use");
        props.setAccessTokenValidityInMinutes(30);
        props.setRefreshTokenValidityInDays(7);
        return props;
    }
    
    @Bean
    @Primary
    public TokenRepository tokenRepository() {
        return Mockito.mock(TokenRepository.class);
    }
    
    @Bean
    @Primary
    public CustomUserDetailsService customUserDetailsService(UserDetailsService userDetailsService) {
        CustomUserDetailsService mockService = Mockito.mock(CustomUserDetailsService.class);
        Mockito.when(mockService.loadUserByUsername(Mockito.anyString()))
            .thenAnswer(invocation -> userDetailsService.loadUserByUsername(invocation.getArgument(0)));
        return mockService;
    }
    
    @Bean
    @Primary
    public JwtService jwtService(JwtProperties jwtProperties, CustomUserDetailsService userDetailsService, TokenRepository tokenRepository) {
        JwtService mockService = Mockito.mock(JwtService.class);
        Mockito.when(mockService.extractUsername(Mockito.anyString())).thenReturn("testuser");
        Mockito.when(mockService.validateToken(Mockito.anyString(), Mockito.any(UserDetails.class))).thenReturn(true);
        return mockService;
    }
    
    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
    
    @Bean 
    @Primary
    public JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint(ObjectMapper objectMapper) {
        return new JwtAuthenticationEntryPoint(objectMapper);
    }
    
    @Bean
    @Primary
    public JwtAuthenticationFilter jwtAuthenticationFilter(JwtService jwtService, CustomUserDetailsService userDetailsService) {
        return new JwtAuthenticationFilter(jwtService, userDetailsService);
    }
    
    @Bean
    @Primary
    public JwtValidationFilter jwtValidationFilter(JwtService jwtService) {
        return new JwtValidationFilter(jwtService);
    }
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthFilter, JwtAuthenticationEntryPoint entryPoint) throws Exception {
        http.csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(new AntPathRequestMatcher("/api/test/public")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/api/test/admin")).hasRole("ADMIN")
                .requestMatchers(new AntPathRequestMatcher("/api/test/user")).hasRole("USER")
                .anyRequest().authenticated())
            .exceptionHandling(except -> except.authenticationEntryPoint(entryPoint))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .addFilterBefore(jwtAuthFilter, org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
} 