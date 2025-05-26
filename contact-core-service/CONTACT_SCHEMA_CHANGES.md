# Contact Core Service Schema Changes

## Overview
This document outlines the changes required for the Contact Core Service to support the new database schema organization that separates microservice concerns and maintains tenant isolation.

## Changes Required

### 1. Database Schema Structure

#### Current Structure:
- Single schema for all contact tables
- Schema-per-tenant approach without service boundaries
- All tables in the same namespace

#### New Structure:
- `public` schema: Tenant registry and system-wide information
- `contact` schema: Shared reference data (contact types, tag categories, etc.)
- `tenant_<id>` schemas: Tenant-specific data with namespaces
  - `contact_data.contacts`: Tenant-specific contact records
  - `contact_data.contact_tags`: Tenant-specific contact tag relationships
  - etc.

### 2. Entity Class Changes

#### Reference Entities (contact schema)
```java
@Entity
@Table(name = "contact_types", schema = "contact")
public class ContactType {
    // Class definition
}

@Entity
@Table(name = "tag_categories", schema = "contact")
public class TagCategory {
    // Class definition
}
```

#### Tenant Entities (tenant_<id> schema)
```java
@Entity
@Table(name = "contact_data.contacts")
public class Contact {
    // Class definition
}

@Entity
@Table(name = "contact_data.contact_tags")
public class ContactTag {
    // Class definition
}
```

### 3. Database Migration Changes

#### New Migration Structure:
```
src/main/resources/db/migration/
├── public/                     # Public schema migrations
│   └── V1__create_tenant_registry.sql
├── contact/                    # Contact service schema migrations
│   └── V1__create_contact_reference_tables.sql
└── tenant/                     # Tenant schema template migrations
    └── contact/                # Contact service tenant tables
        └── V1__create_contact_tables.sql
```

### 4. Configuration Changes

#### Flyway Configuration
```yaml
spring:
  flyway:
    enabled: true
    baseline-on-migrate: true
    locations: 
      - classpath:db/migration/public
      - classpath:db/migration/contact
      - classpath:db/migration/tenant/contact
    schemas: public,contact
    create-schemas: true
    validate-on-migrate: true
```

#### Multi-tenant Configuration
```yaml
multitenancy:
  entity:
    packages: com.salesflow.contact.domain
  namespace: contact_data
  schemas: contact,public
```

### 5. Repository Changes
- Update repository interfaces to reference the new schema structure
- Add schema-specific queries where needed

### 6. Service Layer Changes
- Update service implementations to handle the new schema structure
- Ensure proper tenant context management
- Update transaction management for multi-schema operations

## Implementation Steps

1. **Database Migration Updates**
   - Create new migration files for each schema
   - Remove old migration files
   - Update Flyway configuration

2. **Entity Class Updates**
   - Update schema annotations
   - Modify table references
   - Update entity relationships

3. **Configuration Updates**
   - Update Flyway configuration
   - Update multi-tenant configuration
   - Update Hibernate configuration

4. **Repository Updates**
   - Update repository interfaces
   - Add schema-specific queries
   - Update custom query methods

5. **Service Layer Updates**
   - Update service implementations
   - Add schema-specific operations
   - Update transaction management

6. **Testing**
   - Update test configurations
   - Add schema-specific test cases
   - Verify multi-tenant isolation

## Test Plan

1. **Database Structure**
   - Verify schema creation
   - Verify table creation
   - Verify namespace setup

2. **Multi-tenant Operations**
   - Test tenant creation
   - Test tenant data isolation
   - Test cross-tenant operations

3. **Reference Data**
   - Test reference data access
   - Test reference data updates
   - Test reference data relationships

4. **Integration Tests**
   - Test service integration
   - Test API endpoints
   - Test error handling

## Rollback Plan

If issues arise, the following steps can be taken to roll back changes:

1. **Database Rollback**
   - Restore original schema structure
   - Run original migrations
   - Verify data integrity

2. **Code Rollback**
   - Restore original entity annotations
   - Restore original configurations
   - Restore original service implementations

3. **Configuration Rollback**
   - Restore original Flyway configuration
   - Restore original multi-tenant configuration
   - Restore original Hibernate configuration 