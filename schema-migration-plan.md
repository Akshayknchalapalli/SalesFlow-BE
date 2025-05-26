# SalesFlow Schema Migration Plan

## Overview

This document outlines the plan for migrating the existing SalesFlow database schema to the new organization that supports both microservices and multi-tenancy. The new schema structure separates data by both service and tenant, providing clear boundaries and improved isolation.

## Current vs. New Schema Structure

### Current Structure
- Schema per tenant (`tenant_tenant1`, `tenant_tenant2`, etc.)
- All service tables mixed in each tenant schema
- Authentication data in a separate schema

### New Structure
- Service-specific schemas (`auth`, `contact`, `activity`) for shared data
- Tenant-specific schemas (`tenant_tenant1`, `tenant_tenant2`) for tenant data
- Table namespaces within tenant schemas (`contact_data.*`, `activity_data.*`)
- Public schema for tenant registry and system-wide data

## Migration Steps

### Phase 1: Preparation (Estimated time: 1 day)

1. **Create a backup of the current database**
   ```bash
   pg_dump -h localhost -U postgres -d salesflow > salesflow_backup_before_migration.sql
   ```

2. **Deploy the new schema management utility**
   - Add `SchemaManagementUtility.java` to a common module
   - Update service configurations to use the new utility

3. **Create migration tracking table**
   ```sql
   CREATE TABLE IF NOT EXISTS public.schema_migrations (
       id SERIAL PRIMARY KEY,
       migration_id VARCHAR(100) NOT NULL,
       description TEXT,
       status VARCHAR(20) DEFAULT 'PENDING',
       started_at TIMESTAMP WITH TIME ZONE,
       completed_at TIMESTAMP WITH TIME ZONE,
       error_message TEXT
   );
   ```

### Phase 2: Service Schema Creation (Estimated time: 2 hours)

1. **Initialize public schema**
   - Run the new public schema Flyway migrations
   - Create tenant registry tables

2. **Create service-specific schemas**
   - Create `auth` schema for authentication service
   - Create `contact` schema for contact service
   - Create `activity` schema for activity service

3. **Apply service schema migrations**
   - Run Flyway migrations for each service schema
   - Initialize reference and configuration tables

### Phase 3: Data Migration (Estimated time: 4-8 hours, depending on data volume)

1. **Migrate authentication data**
   - Move users, roles, and permissions to the new `auth` schema
   - Update foreign key constraints as needed

2. **Migrate shared/reference data**
   - Identify and migrate shared reference data to service schemas
   - Create appropriate indices for performance

3. **Migrate tenant data**
   ```sql
   -- Example migration query (for each tenant)
   -- This needs to be executed for each tenant and each data type
   INSERT INTO tenant_tenant1.contact_data.contacts 
   (id, first_name, last_name, email, /* other fields */)
   SELECT id, first_name, last_name, email, /* other fields */
   FROM tenant_tenant1.contacts;
   ```

4. **Validate data migration**
   - Run data validation scripts to ensure all data was migrated correctly
   - Compare record counts between old and new schemas

### Phase 4: Application Updates (Estimated time: 2-3 days)

1. **Update entity mappings**
   - Modify entity classes to reference the new schema structure
   - Update repository interfaces as needed

2. **Update multi-tenant configuration**
   - Modify `CurrentTenantIdentifierResolver` to use the new schema structure
   - Update `MultiTenantConnectionProvider` to handle table namespaces

3. **Update Flyway configuration**
   - Configure Flyway to manage multiple schemas
   - Update migration locations to match the new organization

4. **Update security configuration**
   - Modify database access controls for the new schema structure
   - Update service roles and permissions

### Phase 5: Testing and Verification (Estimated time: 2-3 days)

1. **Run integration tests**
   - Test all CRUD operations against the new schema structure
   - Verify multi-tenant isolation works correctly

2. **Performance testing**
   - Verify query performance with the new schema organization
   - Adjust indices as needed

3. **Load testing**
   - Test the system under load to ensure the new schema organization scales well

### Phase 6: Deployment (Estimated time: 1 day)

1. **Create deployment package**
   - Package all schema changes and application updates

2. **Deploy to staging environment**
   - Test the migration process in staging
   - Verify all services work correctly

3. **Deploy to production**
   - Schedule a maintenance window
   - Perform the migration
   - Verify all services are working correctly

4. **Monitoring**
   - Monitor application performance after the migration
   - Be prepared to roll back if issues arise

## Rollback Plan

In case of problems during or after the migration:

1. **Stop all services**

2. **Restore database from backup**
   ```bash
   psql -h localhost -U postgres -d salesflow < salesflow_backup_before_migration.sql
   ```

3. **Revert application code changes**
   - Roll back to the version before schema changes

4. **Restart services**

## Schema Structure Reference

```
salesflow_db
│
├── public                      # Global configuration and tenant registry
│   ├── tenants                 # Registry of all tenants
│   └── flyway_schema_history   # Flyway migration history
│
├── auth                        # Authentication service schema
│   ├── users                   # User accounts (shared across tenants)
│   ├── roles                   # Role definitions
│   └── user_roles              # User-role mappings
│
├── contact                     # Contact service core schema
│   ├── contact_types           # Shared contact type definitions
│   └── tag_categories          # Shared tag categories
│
├── activity                    # Activity service core schema
│   └── activity_types          # Shared activity type definitions
│
├── tenant_<tenant1>            # Tenant 1 specific schema
│   ├── contact_data            # Tenant 1 contact data
│   │   └── contacts            # Contact records
│   │
│   └── activity_data           # Tenant 1 activity data
│       └── activities          # Activity records
│
└── tenant_<tenant2>            # Tenant 2 specific schema
    ├── contact_data            # Tenant 2 contact data
    │   └── contacts            # Contact records
    │
    └── activity_data           # Tenant 2 activity data
        └── activities          # Activity records
```

## Helpful SQL Queries for Migration

### Identify and Count Records by Tenant

```sql
-- Count contacts by tenant
SELECT 'tenant_' || t.tenant_id AS schema_name, 
       (SELECT COUNT(*) FROM information_schema.tables 
        WHERE table_schema = 'tenant_' || t.tenant_id AND table_name = 'contacts') AS has_contacts_table,
       COALESCE((SELECT COUNT(*) FROM "tenant_" || t.tenant_id || ".contacts"), 0) AS contact_count
FROM public.tenants t
WHERE t.active = true;
```

### Verify Schema Migration

```sql
-- Verify all expected schemas exist
SELECT schema_name 
FROM information_schema.schemata 
WHERE schema_name IN ('public', 'auth', 'contact', 'activity') 
   OR schema_name LIKE 'tenant_%';

-- Verify all expected tables exist in auth schema
SELECT table_name 
FROM information_schema.tables 
WHERE table_schema = 'auth';
```

## Resources and References

- [PostgreSQL Schema Documentation](https://www.postgresql.org/docs/current/ddl-schemas.html)
- [Flyway Multi-Schema Migration](https://flywaydb.org/documentation/concepts/migrations.html#schema)
- [Hibernate Multi-Tenancy](https://docs.jboss.org/hibernate/orm/current/userguide/html_single/Hibernate_User_Guide.html#multitenacy)

## Timeline and Milestones

| Phase | Milestone | Estimated Completion |
|-------|-----------|----------------------|
| 1     | Preparation Complete | Day 1 |
| 2     | Service Schemas Created | Day 1 |
| 3     | Data Migration Complete | Day 2-3 |
| 4     | Application Updates Complete | Day 5-6 |
| 5     | Testing Complete | Day 8-9 |
| 6     | Deployment Complete | Day 10 |