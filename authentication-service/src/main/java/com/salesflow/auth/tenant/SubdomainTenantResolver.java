package com.salesflow.auth.tenant;

import java.util.UUID;

import com.salesflow.auth.config.TenantProperties;
import com.salesflow.auth.repository.TenantRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import lombok.RequiredArgsConstructor;

/**
 * Resolves the tenant ID from the subdomain of the request.
 * For example, for a request to "acme.salesflow.com", the tenant name would be "acme".
 * Also handles localhost with port numbers for local development.
 */
@Component
@RequiredArgsConstructor
public class SubdomainTenantResolver {
    private static final Logger log = LoggerFactory.getLogger(SubdomainTenantResolver.class);
    private static final String LOCALHOST = "localhost";
    
    private final TenantProperties tenantProperties;
    private final JdbcTemplate jdbcTemplate;
    
    /**
     * Resolves the tenant ID and name from the request's subdomain
     */
    public TenantInfo resolveTenant(HttpServletRequest request) {
        String host = request.getServerName();
        String[] parts = host.split("\\.");
        
        // If we have a subdomain, use it as the tenant name
        if (parts.length > 2) {
            String subdomain = parts[0];
            return getTenantInfoByName(subdomain);
        }
        
        // Check for X-Tenant-ID header if header resolution is enabled
        if (tenantProperties.isEnableHeaderResolution() && 
            request.getHeader(tenantProperties.getTenantHeader()) != null) {
            String tenantHeader = request.getHeader(tenantProperties.getTenantHeader());
            try {
                UUID tenantId = UUID.fromString(tenantHeader);
                return getTenantInfoById(tenantId);
            } catch (IllegalArgumentException e) {
                // Not a UUID, try as a name
                return getTenantInfoByName(tenantHeader);
            }
        }
        
        return null;
    }
    
    /**
     * Resolves the tenant ID from the request's subdomain (legacy method)
     */
    public UUID resolveTenantId(HttpServletRequest request) {
        TenantInfo tenantInfo = resolveTenant(request);
        return tenantInfo != null ? tenantInfo.getTenantId() : null;
    }
    
    /**
     * Gets the domain for a tenant
     */
    public String getTenantDomain(UUID tenantId) {
        if (tenantId == null) {
            return null;
        }
        
        String tenantName = getTenantNameById(tenantId);
        if (tenantName == null) {
            return null;
        }
        
        return tenantName + "." + tenantProperties.getBaseDomain();
    }
    
    /**
     * Gets tenant info by tenant ID
     */
    private TenantInfo getTenantInfoById(UUID tenantId) {
        try {
            return jdbcTemplate.queryForObject(
                "SELECT tenant_id, name FROM public.tenants WHERE tenant_id = ? AND active = true",
                (rs, rowNum) -> new TenantInfo(
                    rs.getObject("tenant_id", UUID.class),
                    rs.getString("name")
                ),
                tenantId
            );
        } catch (Exception e) {
            log.warn("Failed to resolve tenant for ID: {}", tenantId, e);
            return null;
        }
    }
    
    /**
     * Gets tenant info by tenant name
     */
    private TenantInfo getTenantInfoByName(String tenantName) {
        try {
            return jdbcTemplate.queryForObject(
                "SELECT tenant_id, name FROM public.tenants WHERE name = ? AND active = true",
                (rs, rowNum) -> new TenantInfo(
                    rs.getObject("tenant_id", UUID.class),
                    rs.getString("name")
                ),
                tenantName
            );
        } catch (Exception e) {
            log.warn("Failed to resolve tenant for name: {}", tenantName, e);
            return null;
        }
    }
    
    /**
     * Gets tenant name by tenant ID
     */
    private String getTenantNameById(UUID tenantId) {
        try {
            return jdbcTemplate.queryForObject(
                "SELECT name FROM public.tenants WHERE tenant_id = ? AND active = true",
                String.class,
                tenantId
            );
        } catch (Exception e) {
            log.warn("Failed to get tenant name for ID: {}", tenantId, e);
            return null;
        }
    }
    
    /**
     * Helper class to store tenant ID and name together
     */
    public static class TenantInfo {
        private final UUID tenantId;
        private final String tenantName;
        
        public TenantInfo(UUID tenantId, String tenantName) {
            this.tenantId = tenantId;
            this.tenantName = tenantName;
        }
        
        public UUID getTenantId() {
            return tenantId;
        }
        
        public String getTenantName() {
            return tenantName;
        }
    }
}