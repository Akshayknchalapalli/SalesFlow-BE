-- V1__create_tenant_registry.sql
-- Creates the tenant registry table in the public schema

-- Create the tenants table to track all tenants in the system
CREATE TABLE IF NOT EXISTS public.tenants (
    id SERIAL PRIMARY KEY,
    tenant_id VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    domain VARCHAR(100),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    active BOOLEAN DEFAULT TRUE,
    tenant_plan VARCHAR(20) DEFAULT 'BASIC',
    max_users INTEGER DEFAULT 5,
    max_storage_mb INTEGER DEFAULT 100,
    owner_email VARCHAR(100),
    contact_person VARCHAR(100),
    contact_phone VARCHAR(20),
    notes TEXT,
    CONSTRAINT tenants_tenant_id_format CHECK (tenant_id ~ '^[a-zA-Z0-9-_]+$')
);

-- Create index on tenant_id for faster lookups
CREATE INDEX IF NOT EXISTS idx_tenants_tenant_id ON public.tenants(tenant_id);
CREATE INDEX IF NOT EXISTS idx_tenants_active ON public.tenants(active);

-- Create a table to store tenant feature flags
CREATE TABLE IF NOT EXISTS public.tenant_features (
    id SERIAL PRIMARY KEY,
    tenant_id VARCHAR(50) NOT NULL,
    feature_key VARCHAR(50) NOT NULL,
    enabled BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (tenant_id) REFERENCES public.tenants(tenant_id) ON DELETE CASCADE,
    CONSTRAINT tenant_features_unique UNIQUE (tenant_id, feature_key)
);

-- Create a table to track tenant schema creation status
CREATE TABLE IF NOT EXISTS public.tenant_schemas (
    id SERIAL PRIMARY KEY,
    tenant_id VARCHAR(50) NOT NULL,
    schema_name VARCHAR(100) NOT NULL,
    service_name VARCHAR(50) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    migration_version VARCHAR(50),
    migration_status VARCHAR(20) DEFAULT 'PENDING',
    last_validation_at TIMESTAMP WITH TIME ZONE,
    FOREIGN KEY (tenant_id) REFERENCES public.tenants(tenant_id) ON DELETE CASCADE,
    CONSTRAINT tenant_schemas_unique UNIQUE (tenant_id, service_name)
);

-- Create audit log for tenant operations
CREATE TABLE IF NOT EXISTS public.tenant_audit_log (
    id SERIAL PRIMARY KEY,
    tenant_id VARCHAR(50) NOT NULL,
    action VARCHAR(50) NOT NULL,
    performed_by VARCHAR(100) NOT NULL,
    performed_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    details JSONB,
    FOREIGN KEY (tenant_id) REFERENCES public.tenants(tenant_id) ON DELETE CASCADE
);

-- Insert default tenant for development
INSERT INTO public.tenants (tenant_id, name, domain, tenant_plan, owner_email)
VALUES ('tenant1', 'Default Tenant', 'tenant1.localhost', 'ENTERPRISE', 'admin@tenant1.com')
ON CONFLICT (tenant_id) DO NOTHING;