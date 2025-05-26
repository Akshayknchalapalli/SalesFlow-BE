package com.salesflow.auth.service;

import com.salesflow.auth.config.FlywayConfig;
import com.salesflow.auth.config.TenantProperties;
import com.salesflow.auth.domain.Role;
import com.salesflow.auth.domain.User;
import com.salesflow.auth.dto.TenantCreationRequest;
import com.salesflow.auth.dto.TenantInfoResponse;
import com.salesflow.auth.repository.RoleRepository;
import com.salesflow.auth.repository.UserRepository;
import com.salesflow.auth.tenant.SubdomainTenantResolver;
import com.salesflow.auth.tenant.TenantContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

@Slf4j
@Service
public class TenantService {
    private static final Pattern TENANT_ID_PATTERN = Pattern.compile("^[a-zA-Z0-9-_]+$");
    private static final int MIN_TENANT_ID_LENGTH = 3;
    private static final int MAX_TENANT_ID_LENGTH = 50;
    
    private JdbcTemplate jdbcTemplate;
    private FlywayConfig flywayConfig;
    private TenantProperties tenantProperties;
    private SubdomainTenantResolver tenantResolver;
    private UserRepository userRepository;
    private RoleRepository roleRepository;
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    public TenantService(
            JdbcTemplate jdbcTemplate,
            @Lazy FlywayConfig flywayConfig,
            TenantProperties tenantProperties,
            SubdomainTenantResolver tenantResolver,
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder) {
        this.jdbcTemplate = jdbcTemplate;
        this.flywayConfig = flywayConfig;
        this.tenantProperties = tenantProperties;
        this.tenantResolver = tenantResolver;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Sets the current tenant ID in the context
     */
    public void setCurrentTenant(UUID tenantId) {
        if (tenantId == null) {
            log.warn("Attempted to set null tenant ID");
            return;
        }
        
        // Get tenant name
        String tenantName = getTenantNameById(tenantId);
        if (tenantName != null) {
            TenantContext.setCurrentTenant(tenantId, tenantName);
            log.debug("Set current tenant to: {} ({})", tenantName, tenantId);
        } else {
            TenantContext.setCurrentTenantId(tenantId);
            log.debug("Set current tenant ID to: {}", tenantId);
        }
    }

    /**
     * Clears the current tenant ID from the context
     */
    public void clearCurrentTenant() {
        TenantContext.clear();
        log.debug("Cleared current tenant");
    }

    /**
     * Gets the current tenant ID from the context
     */
    public UUID getCurrentTenant() {
        return TenantContext.getCurrentTenantId();
    }
    
    /**
     * Gets the current tenant name from the context
     */
    public String getCurrentTenantName() {
        return TenantContext.getCurrentTenantName();
    }
    
    /**
     * Retrieves all tenants from the database
     */
    public List<TenantInfoResponse> getAllTenants() {
        String sql = "SELECT tenant_id, name, created_at, active FROM public.tenants";
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            UUID tenantId = rs.getObject("tenant_id", UUID.class);
            String name = rs.getString("name");
            return TenantInfoResponse.builder()
                    .tenantId(tenantId)
                    .name(name)
                    .domain(tenantResolver.getTenantDomain(tenantId))
                    .active(rs.getBoolean("active"))
                    .plan("standard") // This could come from a separate table in a real implementation
                    .createdAt(rs.getTimestamp("created_at").toLocalDateTime().format(DateTimeFormatter.ISO_DATE_TIME))
                    .build();
        });
    }
    
    /**
     * Gets information about a specific tenant by ID
     */
    public TenantInfoResponse getTenantInfo(UUID tenantId) {
        String sql = "SELECT tenant_id, name, created_at, active FROM public.tenants WHERE tenant_id = ?";
        
        try {
            return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
                UUID tenantIdFromDb = rs.getObject("tenant_id", UUID.class);
                String name = rs.getString("name");
                return TenantInfoResponse.builder()
                        .tenantId(tenantIdFromDb)
                        .name(name)
                        .domain(tenantResolver.getTenantDomain(tenantIdFromDb))
                        .active(rs.getBoolean("active"))
                        .plan("standard") // This could come from a separate table in a real implementation
                        .createdAt(rs.getTimestamp("created_at").toLocalDateTime().format(DateTimeFormatter.ISO_DATE_TIME))
                        .build();
            }, tenantId);
        } catch (Exception e) {
            log.error("Error getting tenant info for ID: {}", tenantId, e);
            return null;
        }
    }
    
    /**
     * Gets information about a specific tenant by name
     */
    public TenantInfoResponse getTenantInfoByName(String tenantName) {
        String sql = "SELECT tenant_id, name, created_at, active FROM public.tenants WHERE name = ?";
        
        try {
            return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
                UUID tenantIdFromDb = rs.getObject("tenant_id", UUID.class);
                String name = rs.getString("name");
                return TenantInfoResponse.builder()
                        .tenantId(tenantIdFromDb)
                        .name(name)
                        .domain(tenantResolver.getTenantDomain(tenantIdFromDb))
                        .active(rs.getBoolean("active"))
                        .plan("standard") // This could come from a separate table in a real implementation
                        .createdAt(rs.getTimestamp("created_at").toLocalDateTime().format(DateTimeFormatter.ISO_DATE_TIME))
                        .build();
            }, tenantName);
        } catch (Exception e) {
            log.error("Error getting tenant info for name: {}", tenantName, e);
            return null;
        }
    }
    
    /**
     * Creates a new tenant with admin user
     */
    @Transactional
    public TenantInfoResponse createTenant(TenantCreationRequest request) {
        // Generate UUID for the tenant if not provided
        UUID tenantId = request.getTenantId() != null ? request.getTenantId() : UUID.randomUUID();
        String tenantName = request.getName();
        
        // Validate tenant name
        if (tenantName == null || tenantName.trim().isEmpty()) {
            throw new IllegalArgumentException("Tenant name cannot be empty");
        }
        
        // Check if tenant already exists by ID
        String checkIdSql = "SELECT COUNT(*) FROM public.tenants WHERE tenant_id = ?";
        int countId = jdbcTemplate.queryForObject(checkIdSql, Integer.class, tenantId);
        if (countId > 0) {
            throw new IllegalArgumentException("Tenant ID already exists: " + tenantId);
        }
        
        // Check if tenant already exists by name
        String checkNameSql = "SELECT COUNT(*) FROM public.tenants WHERE name = ?";
        int countName = jdbcTemplate.queryForObject(checkNameSql, Integer.class, tenantName);
        if (countName > 0) {
            throw new IllegalArgumentException("Tenant name already exists: " + tenantName);
        }
        
        // Register the tenant in the database
        flywayConfig.registerTenant(tenantId, tenantName, tenantResolver.getTenantDomain(tenantId));
        
        // Set up the tenant admin user
        setupTenantAdminUser(tenantId, request.getAdminEmail(), request.getAdminPassword());
        
        // Return the tenant info
        return TenantInfoResponse.builder()
                .tenantId(tenantId)
                .name(tenantName)
                .domain(tenantResolver.getTenantDomain(tenantId))
                .active(true)
                .plan("standard")
                .createdAt(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME))
                .build();
    }
    
    /**
     * Deactivates a tenant
     */
    @Transactional
    public void deactivateTenant(UUID tenantId) {
        String sql = "UPDATE public.tenants SET active = false WHERE tenant_id = ?";
        jdbcTemplate.update(sql, tenantId);
    }
    
    /**
     * Reactivates a tenant
     */
    @Transactional
    public void reactivateTenant(UUID tenantId) {
        String sql = "UPDATE public.tenants SET active = true WHERE tenant_id = ?";
        jdbcTemplate.update(sql, tenantId);
    }
    
    /**
     * Checks if a tenant ID is available
     */
    public boolean isTenantIdAvailable(UUID tenantId) {
        String sql = "SELECT COUNT(*) FROM public.tenants WHERE tenant_id = ?";
        int count = jdbcTemplate.queryForObject(sql, Integer.class, tenantId);
        return count == 0;
    }
    
    /**
     * Checks if a tenant name is available
     */
    public boolean isTenantNameAvailable(String tenantName) {
        String sql = "SELECT COUNT(*) FROM public.tenants WHERE name = ?";
        int count = jdbcTemplate.queryForObject(sql, Integer.class, tenantName);
        return count == 0;
    }
    
    /**
     * Gets tenant name by tenant ID
     */
    public String getTenantNameById(UUID tenantId) {
        try {
            String sql = "SELECT name FROM public.tenants WHERE tenant_id = ?";
            return jdbcTemplate.queryForObject(sql, String.class, tenantId);
        } catch (Exception e) {
            log.warn("Could not find tenant name for ID: {}", tenantId);
            return null;
        }
    }
    
    /**
     * Gets tenant ID by tenant name
     */
    public UUID getTenantIdByName(String tenantName) {
        try {
            String sql = "SELECT tenant_id FROM public.tenants WHERE name = ?";
            return jdbcTemplate.queryForObject(sql, UUID.class, tenantName);
        } catch (Exception e) {
            log.warn("Could not find tenant ID for name: {}", tenantName);
            return null;
        }
    }
    
    /**
     * Gets tenant statistics
     */
    public Map<String, Object> getTenantStatistics(UUID tenantId) {
        // Set the tenant context so the repository queries will be tenant-aware
        setCurrentTenant(tenantId);
        
        try {
            // Count users in the tenant
            long userCount = userRepository.countByTenantId(tenantId);
            
            // Additional statistics would go here in a real implementation
            
            return Map.of(
                "userCount", userCount,
                "isActive", isActive(tenantId)
            );
        } finally {
            // Always clear the tenant context
            clearCurrentTenant();
        }
    }
    
    /**
     * Checks if a tenant is active
     */
    public boolean isActive(UUID tenantId) {
        String sql = "SELECT active FROM public.tenants WHERE tenant_id = ?";
        try {
            return Boolean.TRUE.equals(jdbcTemplate.queryForObject(sql, Boolean.class, tenantId));
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Creates admin user for a new tenant
     */
    private void setupTenantAdminUser(UUID tenantId, String email, String password) {
        // Set the tenant context for user creation
        setCurrentTenant(tenantId);
        
        try {
            // Get tenant name
            String tenantName = getTenantNameById(tenantId);
            
            // Get or create the tenant admin role
            Role tenantAdminRole = roleRepository.findByName("ROLE_TENANT_ADMIN")
                    .orElseGet(() -> {
                        Role role = new Role();
                        role.setName("ROLE_TENANT_ADMIN");
                        return roleRepository.save(role);
                    });
            
            // Get or create the user role
            Role userRole = roleRepository.findByName("ROLE_USER")
                    .orElseGet(() -> {
                        Role role = new Role();
                        role.setName("ROLE_USER");
                        return roleRepository.save(role);
                    });
            
            // Create the tenant admin user
            User adminUser = new User();
            adminUser.setUsername(tenantName + "-admin");
            adminUser.setEmail(email);
            adminUser.setPassword(passwordEncoder.encode(password));
            adminUser.setTenantId(tenantId);
            adminUser.setEnabled(true);
            
            Set<Role> adminRoles = new HashSet<>();
            adminRoles.add(tenantAdminRole);
            adminRoles.add(userRole);  // Admin also has user role
            adminUser.setRoles(adminRoles);
            
            userRepository.save(adminUser);
            
            log.info("Created tenant admin user for tenant: {} ({})", tenantName, tenantId);
        } finally {
            // Always clear the tenant context
            clearCurrentTenant();
        }
    }
    
    /**
     * Validates tenant ID format
     */
    private boolean isValidTenantId(String tenantId) {
        return tenantId != null &&
               tenantId.length() >= MIN_TENANT_ID_LENGTH &&
               tenantId.length() <= MAX_TENANT_ID_LENGTH &&
               TENANT_ID_PATTERN.matcher(tenantId).matches();
    }
    
    /**
     * Validates tenant name format
     */
    private boolean isValidTenantName(String tenantName) {
        // Similar validation to tenant ID but possibly more permissive for human-readable names
        return tenantName != null &&
               tenantName.length() >= MIN_TENANT_ID_LENGTH &&
               tenantName.length() <= MAX_TENANT_ID_LENGTH;
    }
}