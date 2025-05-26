-- V2__update_tenant_schemas.sql
-- Updates tenant schema naming and handling for existing tenants

-- Create function to format tenant name for schema
CREATE OR REPLACE FUNCTION format_schema_name(tenant_name TEXT) RETURNS TEXT AS $$
BEGIN
    -- Convert to lowercase and replace special characters with underscores
    RETURN LOWER(REGEXP_REPLACE(tenant_name, '[^a-zA-Z0-9]', '_', 'g'));
END;
$$ LANGUAGE plpgsql;

-- Add migration_status column if it doesn't exist
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT FROM information_schema.columns 
        WHERE table_schema = 'public' 
        AND table_name = 'tenant_schemas'
        AND column_name = 'migration_status'
    ) THEN
        ALTER TABLE public.tenant_schemas ADD COLUMN migration_status VARCHAR(50);
    END IF;
END
$$;

-- Create temporary table to track schema renames
CREATE TEMP TABLE schema_renames (
    tenant_id UUID,
    old_schema_name VARCHAR(100),
    new_schema_name VARCHAR(100)
);

-- For each tenant, generate new schema name based on tenant name
INSERT INTO schema_renames (tenant_id, old_schema_name, new_schema_name)
SELECT 
    t.tenant_id,
    'tenant_' || REPLACE(CAST(t.tenant_id AS TEXT), '-', '_') AS old_schema_name,
    'tenant_' || format_schema_name(t.name) AS new_schema_name
FROM 
    public.tenants t
WHERE 
    EXISTS (
        SELECT 1 FROM information_schema.schemata 
        WHERE schema_name = 'tenant_' || REPLACE(CAST(t.tenant_id AS TEXT), '-', '_')
    );

-- Rename schemas and update tenant_schemas table
DO $$
DECLARE
    rename_rec RECORD;
BEGIN
    FOR rename_rec IN SELECT * FROM schema_renames LOOP
        -- Only rename if old schema exists and new schema doesn't
        IF EXISTS (
            SELECT 1 FROM information_schema.schemata 
            WHERE schema_name = rename_rec.old_schema_name
        ) AND NOT EXISTS (
            SELECT 1 FROM information_schema.schemata 
            WHERE schema_name = rename_rec.new_schema_name
        ) THEN
            EXECUTE 'ALTER SCHEMA ' || quote_ident(rename_rec.old_schema_name) || 
                    ' RENAME TO ' || quote_ident(rename_rec.new_schema_name);
                    
            -- Update or insert record in tenant_schemas
            INSERT INTO public.tenant_schemas (
                tenant_id, schema_name, service_name, migration_status, 
                created_at, updated_at, last_validation_at
            )
            VALUES (
                rename_rec.tenant_id, 
                rename_rec.new_schema_name, 
                'auth', 
                'COMPLETED',
                NOW(),
                NOW(),
                NOW()
            )
            ON CONFLICT (tenant_id, service_name) 
            DO UPDATE SET 
                schema_name = rename_rec.new_schema_name,
                migration_status = 'COMPLETED',
                updated_at = NOW();
                
            RAISE NOTICE 'Renamed schema % to %', 
                rename_rec.old_schema_name, rename_rec.new_schema_name;
        ELSE
            RAISE NOTICE 'Skipped renaming schema % to % (source not found or target exists)', 
                rename_rec.old_schema_name, rename_rec.new_schema_name;
        END IF;
    END LOOP;
END
$$;

-- Insert audit log entry for the schema renaming
INSERT INTO public.tenant_audit_logs (tenant_id, action, actor, details)
SELECT 
    tenant_id, 
    'SCHEMA_RENAMED', 
    'SYSTEM', 
    json_build_object(
        'old_schema_name', old_schema_name,
        'new_schema_name', new_schema_name,
        'migration_time', NOW()
    )
FROM 
    schema_renames;

-- Drop temporary table
DROP TABLE schema_renames;

-- Clean up
DROP FUNCTION IF EXISTS format_schema_name(TEXT);