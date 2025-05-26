package com.salesflow.auth.tenant;

import java.util.UUID;

/**
 * ThreadLocal-based storage for the current tenant information.
 * This allows the tenant ID and name to be available throughout the request
 * processing lifecycle without having to pass them as parameters.
 */
public class TenantContext {
    private static final ThreadLocal<UUID> CURRENT_TENANT_ID = new ThreadLocal<>();
    private static final ThreadLocal<String> CURRENT_TENANT_NAME = new ThreadLocal<>();
    
    /**
     * Sets the current tenant ID for the current thread.
     *
     * @param tenantId The tenant ID to set
     */
    public static void setCurrentTenantId(UUID tenantId) {
        CURRENT_TENANT_ID.set(tenantId);
    }
    
    /**
     * Sets the current tenant name for the current thread.
     *
     * @param tenantName The tenant name to set
     */
    public static void setCurrentTenantName(String tenantName) {
        CURRENT_TENANT_NAME.set(tenantName);
    }
    
    /**
     * Sets both tenant ID and name for the current thread.
     *
     * @param tenantId The tenant ID to set
     * @param tenantName The tenant name to set
     */
    public static void setCurrentTenant(UUID tenantId, String tenantName) {
        CURRENT_TENANT_ID.set(tenantId);
        CURRENT_TENANT_NAME.set(tenantName);
    }

    /**
     * Gets the current tenant ID for the current thread.
     *
     * @return The current tenant ID or null if not set
     */
    public static UUID getCurrentTenantId() {
        return CURRENT_TENANT_ID.get();
    }
    
    /**
     * Gets the current tenant name for the current thread.
     *
     * @return The current tenant name or null if not set
     */
    public static String getCurrentTenantName() {
        return CURRENT_TENANT_NAME.get();
    }
    
    /**
     * Legacy method for backward compatibility.
     * Gets the current tenant ID for the current thread.
     *
     * @return The current tenant ID or null if not set
     */
    public static UUID getCurrentTenant() {
        return getCurrentTenantId();
    }

    /**
     * Clears the current tenant information from the thread.
     * This should be called at the end of request processing
     * to prevent memory leaks.
     */
    public static void clear() {
        CURRENT_TENANT_ID.remove();
        CURRENT_TENANT_NAME.remove();
    }
}