# Contact Core Service Schema Update Guide

## Overview

This guide provides instructions for updating the Contact Core Service to use the new database schema organization that separates microservice concerns and maintains tenant isolation. The new structure uses service-specific schemas for shared data and tenant-specific schemas with namespaces for tenant-isolated data.

## Schema Structure Changes

### Current Structure
- Single schema for all contact tables
- Schema-per-tenant approach without service boundaries
- All tables in the same namespace

### New Structure
- `public` schema: Tenant registry information
- `contact` schema: Shared reference data (contact types, tag categories, etc.)
- `tenant_<tenant-id>` schemas: Tenant-specific data with namespaces
  - `contact_data.contacts`: Tenant-specific contact records
  - `contact_data.contact_tags`: Tenant-specific contact tag relationships
  - etc.

## Step 1: Database Migration Updates

### Create New Directory Structure

Create the following directory structure for the new migrations:

```
src/main/resources/db/migration/
├── contact/                    # Contact service schema migrations
│   ├── V1__create_contact_reference_tables.sql
│   └── ...
└── tenant/
    └── contact/                # Contact service tenant tables
        ├── V1__create_contact_tables.sql
        └── ...
```

### Delete Existing Migration Files

The following files should be moved/deleted as they are no longer needed in their current form:

- All V*.sql files in `src/main/resources/db/migration/`

### Add New Migration Files

Create the following new migration files:

1. `src/main/resources/db/migration/contact/V1__create_contact_reference_tables.sql`
   - Creates the contact service schema
   - Adds reference tables (contact types, tag categories, etc.)

2. `src/main/resources/db/migration/tenant/contact/V1__create_contact_tables.sql`
   - Creates contact_data namespace in tenant schemas
   - Adds tenant-specific contact tables

## Step 2: Entity Class Updates

Update the following entity classes to reference the new schema structure:

1. Reference/shared entities (e.g., ContactType, TagCategory):
   ```java
   @Entity
   @Table(name = "contact_types", schema = "contact")
   public class ContactType {
       // ...
   }
   ```

2. Tenant-specific entities (e.g., Contact, ContactTag):
   ```java
   @Entity
   @Table(name = "contact_data.contacts")
   public class Contact {
       // ...
   }
   ```

## Step 3: Multi-Tenant Configuration Updates

1. Update `TenantContext.java` to maintain the current tenant ID
2. Update `CurrentTenantIdentifierResolver.java` to use the new schema naming convention
3. Update `MultiTenantConnectionProvider.java` to handle the new schema structure
4. Update `HibernateConfig.java` to configure the multi-tenant strategy

## Step 4: Create Schema Management Utility

Create a `SchemaManagementUtility.java` class that:
1. Initializes the contact schema for shared reference data
2. Creates and initializes tenant schemas with contact_data namespace
3. Registers tenant schemas in the public.tenant_schemas table

## Step 5: Update Repository Classes

If any repositories contain custom queries, update them to reference the new schema structure:

```java
@Query(value = "SELECT c FROM Contact c WHERE c.email = :email")
Optional<Contact> findByEmail(@Param("email") String email);
```

## Step 6: Update Tests

Update test configurations and test cases to work with the new schema structure:

1. Update `application-test.yml` to configure test schema settings
2. Update test data setup to use the new schema structure
3. Modify integration tests to verify the new multi-tenant isolation

## Step 7: Testing the Changes

1. **Database Structure**

   After starting the application, verify the new schema structure:
   
   ```sql
   -- Verify schemas exist
   SELECT schema_name FROM information_schema.schemata 
   WHERE schema_name IN ('public', 'contact', 'tenant_tenant1');
   
   -- Verify contact schema tables
   SELECT table_name FROM information_schema.tables 
   WHERE table_schema = 'contact';
   
   -- Verify tenant tables with namespace
   SELECT table_name FROM information_schema.tables 
   WHERE table_schema = 'tenant_tenant1' AND table_name LIKE 'contact_data.%';
   ```

2. **Contact Operations**

   Verify that contact CRUD operations work correctly:
   
   ```bash
   # Create a contact in tenant1
   curl -X POST http://tenant1.localhost:8080/api/contacts \
     -H "Content-Type: application/json" \
     -H "Authorization: Bearer $TOKEN" \
     -d '{
       "firstName": "John",
       "lastName": "Doe",
       "email": "john@example.com"
     }'
   
   # Get contacts from tenant1
   curl -X GET http://tenant1.localhost:8080/api/contacts \
     -H "Authorization: Bearer $TOKEN"
   ```

## Key Benefits of the New Schema Structure

1. **Clear Service Boundaries**: Contact reference data is in its own dedicated schema
2. **Strong Tenant Isolation**: Each tenant's data is in a separate schema
3. **Organized Table Structure**: Tables within tenant schemas use namespaces for clarity
4. **Improved Performance**: Better indexing and query optimization opportunities
5. **Enhanced Security**: Schema-level permissions for better access control

## Next Steps

1. Add or update documentation about the schema structure
2. Update any client applications or services that interact with contact data
3. Consider adding schema validation utilities
4. Set up monitoring for the new schema structure

## Rollback Procedure

If issues arise, you can roll back to the previous schema structure:

1. Restore the original migration files
2. Revert entity class schema annotations
3. Revert the multi-tenant configuration
4. Restore the database from backup if needed

## Questions?

If you have any questions about the schema update, please contact the architecture team.