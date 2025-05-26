-- V1__create_tenant_registry.sql
-- Creates the tenant registry table and tenant schema tracking tables

-- Create the tenants table for tenant registry
CREATE TABLE IF NOT EXISTS public.tenants (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL UNIQUE,
    domain VARCHAR(255),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    tenant_plan VARCHAR(50),
    max_users INTEGER,
    max_storage_mb INTEGER,
    owner_email VARCHAR(255),
    contact_person VARCHAR(255),
    contact_phone VARCHAR(50),
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- Create an index on tenant_id for fast lookups
CREATE INDEX idx_tenants_tenant_id ON public.tenants(tenant_id);
CREATE INDEX idx_tenants_name ON public.tenants(name);

-- Create the tenant_schemas table for tracking tenant schemas
CREATE TABLE IF NOT EXISTS public.tenant_schemas (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES public.tenants(tenant_id),
    schema_name VARCHAR(100) NOT NULL,
    service_name VARCHAR(50) NOT NULL,
    migration_version VARCHAR(100),
    migration_status VARCHAR(50),
    last_validation_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    UNIQUE(tenant_id, service_name)
);

-- Create an index on tenant_id for fast lookups
CREATE INDEX idx_tenant_schemas_tenant_id ON public.tenant_schemas(tenant_id);

-- Create tenant audit log table
CREATE TABLE IF NOT EXISTS public.tenant_audit_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES public.tenants(tenant_id),
    action VARCHAR(50) NOT NULL,
    actor VARCHAR(255),
    details JSONB,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- Create an index on tenant_id for fast lookups
CREATE INDEX idx_tenant_audit_logs_tenant_id ON public.tenant_audit_logs(tenant_id);

-- Create tenant features table
CREATE TABLE IF NOT EXISTS public.tenant_features (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES public.tenants(tenant_id),
    feature_key VARCHAR(50) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    config JSONB,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    UNIQUE(tenant_id, feature_key)
);

-- Create an index on tenant_id for fast lookups
CREATE INDEX idx_tenant_features_tenant_id ON public.tenant_features(tenant_id);