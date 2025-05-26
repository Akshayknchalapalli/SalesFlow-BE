package com.salesflow.contact.config.tenant;

import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.util.UUID;

/**
 * Resolves the current tenant identifier for Hibernate multi-tenancy.
 * This implementation uses the ThreadLocal TenantContext to determine
 * which tenant schema should be used for database operations.
 * 
 * For contact-specific tables, the schema is tenant_X with contact_data namespace.
 */
@Component
public class ContactTenantIdentifierResolver implements CurrentTenantIdentifierResolver {

    @Value("${multitenancy.namespace}")
    private String namespace;

    @Override
    public String resolveCurrentTenantIdentifier() {
        UUID tenantId = TenantContext.getCurrentTenant();
        return tenantId != null ? "tenant_" + tenantId.toString() : "public";
    }

    @Override
    public boolean validateExistingCurrentSessions() {
        return true;
    }
}