# Multi-Tenant Implementation in Authentication Service

## Overview

This document outlines the implementation of multi-tenant support in the SalesFlow authentication service. The implementation follows a schema-based multi-tenancy approach where:

- Each tenant has its own dedicated database schema
- Schemas are named based on tenant names rather than IDs for better readability
- All entities use UUIDs/GUIDs for primary keys and relationships

## Key Components

### 1. Tenant Identification and Context

- **TenantContext**: ThreadLocal storage for tenant information (ID and name)
- **SubdomainTenantResolver**: Resolves tenant information from request subdomains or headers
- **TenantFilter**: Web filter that sets the tenant context based on incoming requests

### 2. Schema Management

- **FlywayConfig**: Manages schema creation and migration for all tenants
  - Uses tenant names for schema creation (e.g., `tenant_acme` instead of `tenant_123e4567...`)
  - Maintains a record of tenant schemas in the `tenant_schemas` table

### 3. Database Structure

- **Public Schema**: Contains tenant registry and shared configuration
  - `tenants`: Registry of all tenants with UUID IDs and human-readable names
  - `tenant_schemas`: Records of all tenant schemas and their migration status
  
- **Auth Schema**: Contains authentication service shared tables
  - `users`: User accounts with references to tenant IDs
  - `roles`: Role definitions
  - `user_roles`: User-role mappings

- **Tenant Schemas**: Each tenant gets a dedicated schema named `tenant_<tenant_name>`
  - Contains tenant-specific tables like preferences, login history, etc.

## Implementation Details

### Tenant Schema Naming

Schemas are named using tenant names instead of IDs:
- Format: `tenant_<formatted_tenant_name>`
- Example: For tenant "Acme Corp", schema would be `tenant_acme_corp`

The tenant name is sanitized by:
1. Converting to lowercase
2. Replacing spaces and special characters with underscores
3. Limiting to a reasonable length to comply with database identifier limits

### Tenant Context Management

The tenant context is:
1. Resolved from the subdomain or header in the TenantFilter
2. Stored in TenantContext as both ID and name
3. Used throughout the request lifecycle
4. Cleared after request processing to prevent memory leaks

### UUID/GUID Usage

All entities use UUIDs instead of incremental IDs:
- `@GeneratedValue(strategy = GenerationType.UUID)` annotation is used for ID generation
- Foreign keys and relationships use UUID types
- Tenant identification across services uses UUIDs for consistency

## Migration Scripts

The service uses Flyway with structured migration scripts:

- **Public Schema**: `/db/migration/public/V1__create_tenant_registry.sql`
- **Auth Schema**: `/db/migration/auth/V1__create_auth_tables.sql`
- **Tenant Schema Template**: `/db/migration/tenant/V1__create_tenant_tables.sql`

## API Changes

Tenant-related APIs now support:
- Creating tenants with unique names
- Retrieving tenant information by name or ID
- Resolving tenant context from subdomain (e.g., `acme.salesflow.com`)
- Resolving tenant context from header (`X-Tenant-ID`)

## Testing Multi-Tenancy

To test the multi-tenant setup:

1. Register tenants with unique names
2. Verify schema creation with proper naming
3. Create users for each tenant
4. Test data isolation between tenants
5. Use different subdomains to access tenant-specific data

## Implementation Notes

This implementation uses a simplified approach where:

1. Tenant isolation is maintained primarily through application logic
2. Each service manages its own tenant context
3. We don't rely on Hibernate's multi-tenancy features
4. Flyway handles schema creation and migration

## Security Considerations

- Tenant context is validated on each request
- Cross-tenant data access is prevented through application logic
- Every database operation considers the current tenant context