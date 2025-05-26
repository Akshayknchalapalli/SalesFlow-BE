package com.salesflow.contact.config.tenant;

import org.springframework.util.Assert;
import java.util.UUID;

/**
 * ThreadLocal-based implementation of tenant context.
 * This class provides methods to set and get the current tenant identifier
 * for the current thread of execution.
 */
public class TenantContext {
    private static final ThreadLocal<UUID> CURRENT_TENANT = new ThreadLocal<>();

    private TenantContext() {
        // Prevent instantiation
    }

    /**
     * Sets the current tenant identifier for the current thread.
     *
     * @param tenantId the tenant identifier to set
     */
    public static void setCurrentTenant(UUID tenantId) {
        Assert.notNull(tenantId, "Tenant ID cannot be null");
        CURRENT_TENANT.set(tenantId);
    }

    /**
     * Gets the current tenant identifier for the current thread.
     *
     * @return the current tenant identifier, or null if not set
     */
    public static UUID getCurrentTenant() {
        return CURRENT_TENANT.get();
    }

    /**
     * Clears the current tenant identifier for the current thread.
     */
    public static void clear() {
        CURRENT_TENANT.remove();
    }
}