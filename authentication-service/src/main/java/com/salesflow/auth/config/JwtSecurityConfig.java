package com.salesflow.auth.config;

import com.salesflow.auth.service.JwtAuthenticationFilter;
import com.salesflow.auth.service.JwtAuthenticationEntryPoint;
import com.salesflow.auth.filter.TenantValidationFilter;
import com.salesflow.auth.filter.TenantContextFilter;
import com.salesflow.auth.filter.TenantDebugFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@Profile("!test")
public class JwtSecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final TenantValidationFilter tenantValidationFilter;
    private final TenantContextFilter tenantContextFilter;
    private final TenantDebugFilter tenantDebugFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .exceptionHandling(exception -> exception
                .authenticationEntryPoint(jwtAuthenticationEntryPoint))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/register", "/api/auth/login", "/api/auth/refresh").permitAll()
                .requestMatchers("/api/auth/validate").authenticated()
                .requestMatchers("/api/admin/tenants/**").hasRole("ADMIN")
                .requestMatchers("/api/auth/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/tenants/**").hasAnyRole("ADMIN", "TENANT_ADMIN")
                .requestMatchers("/api/auth/tenant/**").hasAnyRole("ADMIN", "TENANT_ADMIN")
                .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/api-docs/**", "/v3/api-docs/**").permitAll()
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .addFilterBefore(tenantDebugFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(tenantContextFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(tenantValidationFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // Allow requests from any tenant subdomain
        configuration.setAllowedOriginPatterns(Arrays.asList(
            "http://localhost:*",
            "http://*.localhost:*", 
            "https://*.salesflow.com", 
            "http://*.salesflow.com"
        ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With", "X-Tenant-ID", "Host"));
        configuration.setExposedHeaders(Arrays.asList("Authorization", "X-Tenant-ID"));
        configuration.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
} 