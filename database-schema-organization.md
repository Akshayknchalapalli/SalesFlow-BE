# SalesFlow Database Schema Organization

## Table of Contents
1. [Introduction](#introduction)
2. [Schema Organization Approach](#schema-organization-approach)
3. [Schema Structure](#schema-structure)
4. [Schema Naming Conventions](#schema-naming-conventions)
5. [Flyway Migration Structure](#flyway-migration-structure)
6. [Database Access Patterns](#database-access-patterns)
7. [Implementation Steps](#implementation-steps)

## Introduction

This document outlines the approach for organizing database schemas in the SalesFlow application to support both microservice boundaries and multi-tenancy requirements. The goal is to create a clear separation of data for different microservices while maintaining tenant isolation.

## Schema Organization Approach

After evaluating different approaches, we've selected a **hybrid schema organization strategy** that combines service-specific schemas with tenant-specific schemas:

1. **Service-specific schemas** - Core schemas for each microservice that contain service-specific tables and shared data
2. **Tenant-specific schemas** - Separate schemas for each tenant that contain tenant-specific data
3. **Public schema** - Contains global configuration and tenant registry

This approach provides:
- Clear separation between microservices
- Strong tenant data isolation
- Logical organization of database objects
- Easier management of migrations
- Compatibility with the existing code structure

## Schema Structure

The database will be organized with the following schema structure:

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
│   ├── user_roles              # User-role mappings
│   └── flyway_schema_history   # Auth service migration history
│
├── contact                     # Contact service core schema
│   ├── contact_types           # Shared contact type definitions
│   ├── tag_categories          # Shared tag categories
│   └── flyway_schema_history   # Contact service migration history
│
├── activity                    # Activity service core schema
│   ├── activity_types          # Shared activity type definitions
│   └── flyway_schema_history   # Activity service migration history
│
├── tenant_<tenant1>            # Tenant 1 specific schema
│   ├── contact_data            # Tenant 1 contact data
│   │   ├── contacts            # Contact records
│   │   ├── contact_tags        # Contact tag relationships
│   │   └── contact_details     # Extended contact details
│   │
│   ├── activity_data           # Tenant 1 activity data
│   │   ├── activities          # Activity records
│   │   └── activity_details    # Activity details
│   │
│   └── flyway_schema_history   # Tenant 1 migration history
│
├── tenant_<tenant2>            # Tenant 2 specific schema
    ├── contact_data            # Tenant 2 contact data
    │   ├── contacts            # Contact records
    │   ├── contact_tags        # Contact tag relationships
    │   └── contact_details     # Extended contact details
    │
    ├── activity_data           # Tenant 2 activity data
    │   ├── activities          # Activity records
    │   └── activity_details    # Activity details
    │
    └── flyway_schema_history   # Tenant 2 migration history
```

## Schema Naming Conventions

- **Service schemas**: Use the service name (e.g., `auth`, `contact`, `activity`)
- **Tenant schemas**: Use the prefix `tenant_` followed by the tenant identifier (e.g., `tenant_acme`, `tenant_globex`)
- **Table namespaces**: Within tenant schemas, use prefixes to indicate service ownership (e.g., `contact_data`, `activity_data`)
- **Shared tables**: Placed in service-specific schemas with no tenant identifier

## Flyway Migration Structure

Each service will have its own set of migrations, organized as follows:

```
src/main/resources/db/migration/
│
├── public/                     # Public schema migrations
│   ├── V1__create_tenant_registry.sql
│   └── ...
│
├── auth/                       # Auth service schema migrations
│   ├── V1__create_users_table.sql
│   ├── V2__create_roles_table.sql
│   └── ...
│
├── contact/                    # Contact service schema migrations
│   ├── V1__create_contact_types.sql
│   └── ...
│
├── activity/                   # Activity service schema migrations
│   ├── V1__create_activity_types.sql
│   └── ...
│
├── tenant/                     # Tenant schema template migrations
│   ├── contact/                # Contact service tenant tables
│   │   ├── V1__create_contacts_table.sql
│   │   └── ...
│   │
│   ├── activity/               # Activity service tenant tables
│   │   ├── V1__create_activities_table.sql
│   │   └── ...
│   │
│   └── combined/               # Migrations that span multiple services
│       ├── V1__create_initial_tables.sql
│       └── ...
```

## Database Access Patterns

### Authentication Service

- Reads/writes to the `auth` schema for user management
- Only needs read access to `public.tenants` for tenant validation
- No direct access to tenant schemas

### Contact Service

- Reads from `contact` schema for shared data
- Reads/writes to `tenant_<id>.contact_data.*` tables based on current tenant context

### Activity Service

- Reads from `activity` schema for shared data
- Reads/writes to `tenant_<id>.activity_data.*` tables based on current tenant context

## Implementation Steps

1. **Update Flyway Configuration**

   Modify each service's Flyway configuration to handle multiple schema targets:

   ```java
   @Bean
   public Flyway flyway(DataSource dataSource) {
       Flyway.configure()
           .dataSource(dataSource)
           .schemas("public", "auth")
           .locations("classpath:db/migration/public", "classpath:db/migration/auth")
           .baselineOnMigrate(true)
           .load()
           .migrate();
       
       return Flyway.configure()
           .dataSource(dataSource)
           .schemas("public", "auth")
           .locations("classpath:db/migration/public", "classpath:db/migration/auth")
           .baselineOnMigrate(true)
           .load();
   }
   ```

2. **Update Tenant Schema Creation**

   Modify the tenant creation process to create the tenant schema with appropriate table namespaces:

   ```java
   public void createTenantSchema(String tenantId) {
       String schema = "tenant_" + tenantId.toLowerCase().replace("-", "_");
       
       // Create schema
       try (Connection conn = dataSource.getConnection();
            Statement stmt = conn.createStatement()) {
           stmt.execute("CREATE SCHEMA IF NOT EXISTS " + schema);
       }
       
       // Run tenant migrations for contact data
       Flyway.configure()
           .dataSource(dataSource)
           .schemas(schema)
           .locations("classpath:db/migration/tenant/contact")
           .baselineOnMigrate(true)
           .load()
           .migrate();
       
       // Run tenant migrations for activity data
       Flyway.configure()
           .dataSource(dataSource)
           .schemas(schema)
           .locations("classpath:db/migration/tenant/activity")
           .baselineOnMigrate(true)
           .load()
           .migrate();
       
       // Run combined tenant migrations
       Flyway.configure()
           .dataSource(dataSource)
           .schemas(schema)
           .locations("classpath:db/migration/tenant/combined")
           .baselineOnMigrate(true)
           .load()
           .migrate();
   }
   ```

3. **Update Entity Annotations**

   Modify entity classes to specify the schema and table name:

   ```java
   // Authentication service entities (auth schema)
   @Entity
   @Table(name = "users", schema = "auth")
   public class User { ... }
   
   // Contact service shared entities (contact schema)
   @Entity
   @Table(name = "contact_types", schema = "contact")
   public class ContactType { ... }
   
   // Contact service tenant entities (dynamic schema)
   @Entity
   @Table(name = "contact_data.contacts")
   public class Contact { ... }
   ```

4. **Update Hibernate Configuration**

   Modify the Hibernate multi-tenant configuration to handle the schema pattern:

   ```java
   public class CurrentTenantIdentifierResolver implements CurrentTenantIdentifierResolver {
       @Override
       public String resolveCurrentTenantIdentifier() {
           String tenantId = TenantContext.getCurrentTenant();
           if (tenantId != null && !tenantId.isEmpty()) {
               // Format the tenant ID as a schema name
               return "tenant_" + tenantId.toLowerCase().replace("-", "_");
           }
           return "public"; // Default schema
       }
   }
   ```