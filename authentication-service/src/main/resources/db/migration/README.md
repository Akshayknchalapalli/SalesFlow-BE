# Flyway Migration Troubleshooting Guide

## Common Issues

### 1. Migration Checksum Mismatch

```
Migration checksum mismatch for migration version X
-> Applied to database : 12345678
-> Resolved locally    : 87654321
```

This occurs when the migration file content has changed after it was already applied to the database.

### 2. Missing Migrations

```
Detected applied migration not resolved locally: X
```

This happens when the database contains a migration that doesn't exist in your codebase.

### 3. Out of Order Migrations

```
Found migration not applied to database: Y
Bypassed: X
```

This occurs when a lower version migration is added after higher versions were already applied.

## Solutions

### Option 1: Repair Flyway Schema History

Use the standalone repair tool (recommended):

```bash
# Using the standalone repair tool (doesn't require Spring context)
java -cp authentication-service.jar com.salesflow.auth.util.FlywayRepairTool application-dev.yml authentication classpath:db/migration/authentication

# For the public schema
java -cp authentication-service.jar com.salesflow.auth.util.FlywayRepairTool application-dev.yml public classpath:db/migration/public
```

Or use the Spring-based utility script:

```bash
# Using the Spring-based utility script
java -cp authentication-service.jar com.salesflow.auth.util.FlywayRepairCommand repair
```

Or enable automatic repair in application properties:

```yaml
spring:
  flyway:
    repair-on-start: true
```

### Option 2: Disable Validation (Development Only)

In development environments, you can disable validation:

```yaml
spring:
  flyway:
    validate-on-migrate: false
```

### Option 3: Clean Database (Development Only)

**WARNING: This will delete all data!**

For development environments, you can clean the database:

```yaml
spring:
  flyway:
    clean-on-start: true
```

Or manually using the Flyway CLI:

```bash
flyway clean -schemas=authentication,public
```

## Best Practices

1. **Never modify migrations that have been applied to any environment**
   - Always create a new migration to make changes

2. **Use version naming that avoids conflicts**
   - Consider date-based versioning: `V2023_10_15_1__description.sql`

3. **Keep migrations idempotent when possible**
   - Use `IF NOT EXISTS`, `ON CONFLICT DO NOTHING`, etc.

4. **Version control your migrations**
   - Ensure migrations are properly tracked in Git

5. **Test migrations thoroughly before production**
   - Have a staging environment with similar data to production

## Emergency Override

If you need to force Flyway to ignore specific migrations:

```sql
-- Execute directly in your database
UPDATE flyway_schema_history 
SET checksum = 12345678 -- Set to the expected checksum
WHERE version = 'X';
```

### Finding the Correct Checksum

To find the correct checksum for a migration file:

1. Look at the error message:
   ```
   Migration checksum mismatch for migration version X
   -> Applied to database : 12345678
   -> Resolved locally    : 87654321
   ```

2. Use the "Resolved locally" value as the correct checksum.

3. Or use the standalone repair tool which will automatically fix checksums.