# Migration Guide for SalesFlow Database Structure

## Schema-Per-Tenant Migration Strategy

This document outlines how we've structured our database migrations to support a schema-per-tenant model in the SalesFlow application.

## Migration Directory Structure

```
src/main/resources/db/migration/
├── V1__create_auth_tables.sql        # Original migrations (keep these)
├── V2_add_auth_constrinst.sql        # Original migrations (keep these)
├── ... (other original migrations)
├── public/                           # Public schema migrations
│   └── V1__create_tenant_table.sql
├── authentication/                   # Authentication schema migrations
│   └── V1__init_auth_schema.sql
└── tenant/                           # Tenant schema template migrations
    └── V1__init_tenant_schema.sql
```

## Migration Execution Strategy

1. **Original Migrations** (`/db/migration/*.sql`):
   - These are kept for historical purposes
   - They continue to run in the authentication schema
   - Future migrations should use the new structure

2. **Public Schema Migrations** (`/db/migration/public/*.sql`):
   - Applied to the `public` schema
   - Contains tenant registry and shared tables
   - Versioned independently from other schemas

3. **Authentication Schema Migrations** (`/db/migration/authentication/*.sql`):
   - Applied to the `authentication` schema
   - Contains user authentication and authorization tables
   - Versioned independently from other schemas

4. **Tenant Schema Migrations** (`/db/migration/tenant/*.sql`):
   - Templates applied to each new tenant schema
   - Used when creating new tenants
   - Versioned independently from other schemas

## Flyway Configuration

The application is configured to recognize all migration paths:

```yaml
spring:
  flyway:
    enabled: true
    baseline-on-migrate: true
    locations: 
      - classpath:db/migration
      - classpath:db/migration/authentication
      - classpath:db/migration/public
      - classpath:db/migration/tenant
    schemas: authentication, public
    create-schemas: true
    validate-on-migrate: true
    out-of-order: true
```

## Creating New Migrations

When creating new migrations:

1. **Place them in the appropriate directory** based on their purpose
2. **Use versioned filenames** that follow the Flyway convention (e.g., `V3__add_new_feature.sql`)
3. **Keep version numbers sequential** within each directory
4. **Make migrations idempotent** when possible (use `IF NOT EXISTS` clauses)

Example:
```sql
-- File: db/migration/tenant/V2__add_contact_custom_fields.sql
ALTER TABLE contacts ADD COLUMN IF NOT EXISTS custom_field1 VARCHAR(100);
ALTER TABLE contacts ADD COLUMN IF NOT EXISTS custom_field2 VARCHAR(100);
```

## Handling Tenant Provisioning

When a new tenant is provisioned:

1. A new schema named `tenant_[tenant_id]` is created
2. All migrations from the `tenant` directory are applied to this schema
3. The tenant is registered in the `public.tenants` table

This process is handled by the `FlywayConfig` and `TenantService` classes.

## Best Practices

1. **Never modify existing migrations** - Always create new ones
2. **Test migrations thoroughly** before deploying to production
3. **Take database backups** before applying migrations
4. **Monitor migration execution** in logs
5. **Document schema changes** in migration files with comments

## Troubleshooting

If migrations fail:

1. Check the Flyway schema history table in each schema
2. Verify that migration versions are in the correct order
3. Ensure that migrations are idempotent
4. Check for syntax errors or invalid SQL statements

For more information, refer to the Flyway documentation or contact the database administrator.