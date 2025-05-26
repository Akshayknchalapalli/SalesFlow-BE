# SalesFlow Multi-Tenant Architecture Guide

## Table of Contents
1. [Introduction](#introduction)
2. [Architecture Overview](#architecture-overview)
3. [Multi-Tenancy Implementation](#multi-tenancy-implementation)
4. [Service Components](#service-components)
5. [Database Structure](#database-structure)
6. [Local Development Setup](#local-development-setup)
7. [DNS Configuration](#dns-configuration)
8. [Running the System](#running-the-system)
9. [Testing Multi-Tenancy](#testing-multi-tenancy)
10. [Troubleshooting](#troubleshooting)
11. [Advanced Multi-Tenant Concepts](#advanced-multi-tenant-concepts)
12. [Security Considerations](#security-considerations)
13. [Production Deployment](#production-deployment)
14. [Performance Optimization](#performance-optimization)
15. [Monitoring and Maintenance](#monitoring-and-maintenance)

## Introduction

SalesFlow is a comprehensive contact management system built using a microservices architecture with multi-tenant support. This document explains the multi-tenant architecture and provides a step-by-step guide to set up and run the system locally.

Multi-tenancy allows a single instance of the application to serve multiple customer organizations (tenants) while keeping their data isolated from each other. The SalesFlow system implements a schema-based multi-tenancy approach where each tenant's data is stored in a separate schema within the same database.

## Architecture Overview

The SalesFlow system consists of several microservices:

1. **Service Registry** - Eureka server for service discovery
2. **Authentication Service** - Handles user authentication and tenant validation
3. **Contact Core Service** - Manages contact data with multi-tenant support
4. **Contact Activity Service** - Tracks contact interactions
5. **Other services** - Additional functionality modules

The system uses:
- Spring Boot for microservices
- Spring Cloud for service discovery
- Hibernate with schema-based multi-tenancy
- PostgreSQL for data storage
- Redis for caching
- JWT for authentication

## Multi-Tenancy Implementation

SalesFlow implements multi-tenancy at several levels:

### 1. Tenant Identification

Tenants are identified through either:
- Subdomain (e.g., `tenant1.salesflow.com`)
- HTTP header (`X-Tenant-ID`)

The tenant ID is extracted by a filter and stored in a ThreadLocal context for the duration of the request.

### 2. Schema-Based Data Isolation

Each tenant's data is stored in a separate PostgreSQL schema:
- `public` schema - Stores tenant registry information
- `authentication` schema - Stores user credentials and roles
- `tenant_<tenant-id>` schemas - Store tenant-specific data

### 3. Dynamic Schema Creation

When a new tenant is registered, the system:
1. Creates an entry in the tenant registry table
2. Creates a new schema for the tenant
3. Applies Flyway migrations to set up the schema structure

## Service Components

### Authentication Service

- Manages user authentication and authorization
- Handles tenant validation and registration
- Issues JWT tokens with tenant information
- Implements subdomain-based tenant resolution

Key components:
- `SubdomainTenantResolver` - Extracts tenant ID from subdomain
- `TenantContextFilter` - Sets tenant ID in ThreadLocal context
- `TenantValidationFilter` - Validates tenant ID format and existence
- `FlywayConfig` - Manages schema creation and migration

### Contact Core Service

- Manages contact data with multi-tenant isolation
- Uses Hibernate's multi-tenancy support

Key components:
- `MultiTenantConnectionProvider` - Switches database schema per tenant
- `CurrentTenantIdentifierResolver` - Resolves current tenant from context
- `HibernateConfig` - Configures multi-tenancy for Hibernate
- `TenantFilter` - Extracts tenant ID from request

## Database Structure

The database uses the following schema structure:

1. **public schema**
   - `tenants` table - Stores tenant registry information
   - Contains tenant_id, name, and activation status

2. **authentication schema**
   - `users` table - User credentials and profiles
   - `roles` table - Role definitions
   - `user_roles` table - User-role mappings

3. **tenant_<tenant-id> schemas**
   - `contacts` table - Contact information
   - `tags` table - Contact tags
   - `activities` table - Contact activities
   - Other tenant-specific tables

## Local Development Setup

### Prerequisites

1. Java 17 or higher
2. Maven 3.8 or higher
3. Docker and Docker Compose
4. Git

### Step 1: Clone the Repository

```bash
git clone https://github.com/your-org/salesflow.git
cd salesflow
```

### Step 2: Configure Local Environment

Create a `.env` file in the authentication-service directory with the following content:

```
# Database Configuration
DB_URL=jdbc:postgresql://localhost:5432/salesflow
DB_USERNAME=postgres
DB_PASSWORD=postgres
DB_NAME=salesflow
POSTGRES_USER=postgres
POSTGRES_PASSWORD=postgres
POSTGRES_DB=salesflow

# JWT Configuration
JWT_SECRET_KEY=your-secret-key-should-be-at-least-256-bits
JWT_EXPIRATION_TIME=86400000

# Service Discovery
EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://localhost:8761/eureka/
EUREKA_INSTANCE_PREFERIPADDRESS=true
EUREKA_INSTANCE_HOSTNAME=localhost
EUREKA_CLIENT_INITIALINSTANCEINFOREADYTIME=5
EUREKA_CLIENT_REGISTRYFETCHINTERVALSECONDS=5

# Logging Configuration
LOGGING_LEVEL_COM_SALESFLOW_AUTH=DEBUG
LOGGING_LEVEL_ORG_SPRINGFRAMEWORK_SECURITY=INFO
LOGGING_LEVEL_TENANT=DEBUG
LOGGING_LEVEL_FLYWAY=DEBUG

# Multi-tenant Configuration
TENANT_BASE_DOMAIN=salesflow.com
TENANT_LOCAL_MODE=true
TENANT_LOCAL_PORT=8081
TENANT_HEADER=X-Tenant-ID
DEFAULT_TENANT=tenant1

# Flyway Multi-tenant Configuration
FLYWAY_TENANT_LOCATIONS=classpath:db/migration/tenant
FLYWAY_PUBLIC_LOCATIONS=classpath:db/migration/public
FLYWAY_AUTH_LOCATIONS=classpath:db/migration/authentication
```

## DNS Configuration

To test multi-tenancy locally, you need to configure DNS for tenant subdomains. This section provides multiple methods for setting up DNS resolution for local development.

### Method 1: Edit hosts file

This is the simplest method but requires adding entries for each tenant.

#### For macOS/Linux:

1. Open terminal and edit the hosts file:
   ```bash
   sudo nano /etc/hosts
   ```

2. Add the following entries:
   ```
   127.0.0.1   tenant1.localhost
   127.0.0.1   tenant2.localhost
   127.0.0.1   tenant3.localhost
   127.0.0.1   salesflow.localhost
   ```

3. Save the file (Ctrl+O then Enter) and exit (Ctrl+X)

4. Flush DNS cache:
   ```bash
   # macOS
   sudo killall -HUP mDNSResponder
   
   # Ubuntu/Debian
   sudo systemctl restart systemd-resolved
   ```

#### For Windows:

1. Open Command Prompt as Administrator
2. Edit the hosts file:
   ```
   notepad C:\Windows\System32\drivers\etc\hosts
   ```

3. Add the following entries:
   ```
   127.0.0.1   tenant1.localhost
   127.0.0.1   tenant2.localhost
   127.0.0.1   tenant3.localhost
   127.0.0.1   salesflow.localhost
   ```

4. Save the file and exit
5. Flush DNS cache:
   ```
   ipconfig /flushdns
   ```

### Method 2: Use dnsmasq (macOS/Linux)

This method provides wildcard subdomain support, allowing any tenant name to resolve without individual entries.

#### For macOS:

1. Install dnsmasq via Homebrew:
   ```bash
   brew install dnsmasq
   ```

2. Create a configuration file:
   ```bash
   mkdir -p $(brew --prefix)/etc/
   echo 'address=/.localhost/127.0.0.1' > $(brew --prefix)/etc/dnsmasq.conf
   ```

3. Start dnsmasq service:
   ```bash
   sudo brew services start dnsmasq
   ```

4. Configure macOS to use dnsmasq for .localhost domains:
   ```bash
   sudo mkdir -p /etc/resolver
   echo 'nameserver 127.0.0.1' | sudo tee /etc/resolver/localhost
   ```

5. Test configuration:
   ```bash
   ping -c 1 anytenant.localhost
   ```
   
   You should see responses from 127.0.0.1

#### For Ubuntu/Debian:

1. Install dnsmasq:
   ```bash
   sudo apt-get update
   sudo apt-get install dnsmasq
   ```

2. Configure dnsmasq:
   ```bash
   sudo bash -c 'cat > /etc/dnsmasq.d/localhost.conf << EOF
   address=/.localhost/127.0.0.1
   EOF'
   ```

3. Restart dnsmasq:
   ```bash
   sudo systemctl restart dnsmasq
   ```

4. Configure NetworkManager to use dnsmasq:
   ```bash
   sudo bash -c 'cat > /etc/NetworkManager/conf.d/dnsmasq.conf << EOF
   [main]
   dns=dnsmasq
   EOF'
   ```

5. Restart NetworkManager:
   ```bash
   sudo systemctl restart NetworkManager
   ```

### Method 3: Use lvh.me Domain (No Configuration Needed)

If you prefer not to modify your system configuration, you can use the public domain `lvh.me` which is already configured to point to 127.0.0.1 with wildcard subdomain support.

1. Modify your application configuration to use `lvh.me` instead of `localhost`:
   ```
   # In .env file
   TENANT_BASE_DOMAIN=lvh.me
   ```

2. Access the application using:
   - http://tenant1.lvh.me:8081
   - http://tenant2.lvh.me:8081

### Verifying DNS Configuration

To verify your DNS configuration is working correctly:

1. Ping a tenant subdomain:
   ```bash
   ping tenant1.localhost
   ```
   
   Expected output should show replies from 127.0.0.1

2. Test DNS resolution:
   ```bash
   # macOS/Linux
   dig tenant1.localhost
   
   # Windows
   nslookup tenant1.localhost
   ```
   
   The result should show 127.0.0.1 as the resolved IP address

3. Check with curl:
   ```bash
   curl -I http://tenant1.localhost:8081/api/health
   ```
   
   You should receive a HTTP response (not a DNS error)

### Troubleshooting DNS Issues

1. **DNS Resolution Fails**
   - Verify the entries in your hosts file or dnsmasq configuration
   - Try flushing DNS cache again
   - Check if your browser is using a custom DNS (like Google DNS or Cloudflare)
   
2. **Certificate Warnings**
   - Modern browsers enforce HTTPS for .dev domains, use .localhost instead
   - Add a security exception in your browser for development
   - Use a self-signed certificate for local development

3. **Connection Refused**
   - Confirm the application is running on the expected port
   - Check if a firewall is blocking connections
   - Verify the service is listening on all interfaces (0.0.0.0), not just localhost

## Running the System

### Step 1: Start Infrastructure Services

First, we need to start the necessary infrastructure services using Docker Compose:

```bash
cd SalesFlow-BE/authentication-service
docker-compose up -d postgres redis
```

Verify the services are running:
```bash
docker ps
```

You should see both PostgreSQL and Redis containers running. Wait a few seconds for them to initialize completely.

### Step 2: Start Service Registry

The Service Registry (Eureka) is a crucial component that enables service discovery:

```bash
cd SalesFlow-BE/service-registry
mvn clean spring-boot:run
```

Wait for the Eureka server to start. The console should show something like:
```
Started EurekaServerApplication in x.x seconds
```

The Eureka service registry will be available at: http://localhost:8761

Verify it's working by accessing the URL in your browser. You should see the Eureka dashboard with "No instances available" (since we haven't started any services yet).

### Step 3: Start Authentication Service

The Authentication Service handles user login, tenant validation, and JWT token generation:

```bash
cd SalesFlow-BE/authentication-service
mvn clean spring-boot:run
```

Wait for the service to start and register with Eureka. Look for messages like:
```
Started AuthServiceApplication in x.x seconds
DiscoveryClient_AUTH-SERVICE/hostname:auth-service:8081: registering service...
DiscoveryClient_AUTH-SERVICE/hostname:auth-service:8081 - registration status: 204
```

The Authentication service will be available at: http://localhost:8081

Verify it's running with:
```bash
curl http://localhost:8081/actuator/health
```

You should see a response like: `{"status":"UP"}`

Also check the Eureka dashboard at http://localhost:8761 - you should now see the Authentication Service registered.

### Step 4: Start Contact Core Service

The Contact Core Service manages contact data with multi-tenant support:

```bash
cd SalesFlow-BE/contact-core-service
mvn clean spring-boot:run
```

Wait for the service to start and register with Eureka.

The Contact service will be available at: http://localhost:8080

Verify it's running with:
```bash
curl http://localhost:8080/actuator/health
```

You should see a response like: `{"status":"UP"}`

Check the Eureka dashboard again - both services should now be registered.

### Step 5: Start Additional Services

Start any other services required for your use case:

```bash
cd SalesFlow-BE/contact-activity-service
mvn clean spring-boot:run
```

### Step 6: Verify Multi-Tenant Setup

Confirm the multi-tenant configuration is working correctly:

1. Check tenant routing via subdomain:
   ```bash
   curl -I http://tenant1.localhost:8081/actuator/health
   ```

2. Check tenant routing via header:
   ```bash
   curl -I -H "X-Tenant-ID: tenant1" http://localhost:8081/actuator/health
   ```

Both approaches should return a 200 OK response.

### Step 7: Using the Development Environment

For local development, you can use either:

1. Multiple terminal windows (one for each service)
2. IDE run configurations for each service
3. Docker Compose for all services (see the Production Deployment section)

Always start the services in the following order:
1. Infrastructure (PostgreSQL, Redis)
2. Service Registry
3. Authentication Service
4. Other domain services

## Testing Multi-Tenancy

This section provides a step-by-step guide to test the multi-tenant features.

### 1. Register Tenants

First, create tenant organizations using the tenant registration API. This creates the necessary database schemas and configurations for each tenant.

```bash
# Register first tenant
curl -X POST http://localhost:8081/api/auth/tenant/register \
  -H "Content-Type: application/json" \
  -d '{"tenantId": "tenant1", "name": "Tenant 1 Organization"}' \
  | jq

# Register second tenant
curl -X POST http://localhost:8081/api/auth/tenant/register \
  -H "Content-Type: application/json" \
  -d '{"tenantId": "tenant2", "name": "Tenant 2 Organization"}' \
  | jq
```

Expected response:
```json
{
  "tenantId": "tenant1",
  "name": "Tenant 1 Organization",
  "status": "ACTIVE",
  "createdAt": "2023-10-12T14:30:45Z"
}
```

Verify tenant creation by listing all tenants:
```bash
curl -X GET http://localhost:8081/api/auth/admin/tenants \
  -H "Authorization: Bearer <admin-jwt-token>" \
  | jq
```

### 2. Create Tenant Users

Next, register users for each tenant. Note how we're using the tenant subdomain to ensure proper tenant context:

```bash
# Create user for tenant1
curl -X POST http://tenant1.localhost:8081/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "user1",
    "email": "user1@tenant1.com", 
    "password": "securePassword123", 
    "tenantId": "tenant1",
    "firstName": "John",
    "lastName": "Doe"
  }' \
  | jq

# Create user for tenant2
curl -X POST http://tenant2.localhost:8081/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "user2", 
    "email": "user2@tenant2.com", 
    "password": "securePassword123", 
    "tenantId": "tenant2",
    "firstName": "Jane",
    "lastName": "Smith"
  }' \
  | jq
```

Expected response:
```json
{
  "id": 1,
  "username": "user1",
  "email": "user1@tenant1.com",
  "tenantId": "tenant1",
  "enabled": true,
  "roles": ["ROLE_USER"]
}
```

### 3. Login as Tenant User

Now, authenticate users from different tenants to obtain JWT tokens:

```bash
# Login as tenant1 user
tenant1_response=$(curl -X POST http://tenant1.localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "user1", 
    "password": "securePassword123"
  }')
  
# Extract the JWT token
tenant1_token=$(echo $tenant1_response | jq -r '.accessToken')
echo "Tenant1 Token: $tenant1_token"

# Login as tenant2 user
tenant2_response=$(curl -X POST http://tenant2.localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "user2", 
    "password": "securePassword123"
  }')
  
# Extract the JWT token
tenant2_token=$(echo $tenant2_response | jq -r '.accessToken')
echo "Tenant2 Token: $tenant2_token"
```

Verify the tokens are valid:
```bash
curl -X GET http://tenant1.localhost:8081/api/auth/validate \
  -H "Authorization: Bearer $tenant1_token" \
  | jq
```

### 4. Use Services with Tenant Context

Now you can access tenant-specific resources using the JWT token. The system supports two methods for tenant context:

#### Method A: Using Subdomains (Recommended)

This approach uses the subdomain to determine the tenant context:

```bash
# Create a contact in tenant1
curl -X POST http://tenant1.localhost:8080/api/contacts \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $tenant1_token" \
  -d '{
    "firstName": "Alice",
    "lastName": "Johnson",
    "email": "alice@example.com",
    "phone": "555-123-4567"
  }' \
  | jq

# List contacts in tenant1
curl -X GET http://tenant1.localhost:8080/api/contacts \
  -H "Authorization: Bearer $tenant1_token" \
  | jq

# Create a contact in tenant2
curl -X POST http://tenant2.localhost:8080/api/contacts \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $tenant2_token" \
  -d '{
    "firstName": "Bob",
    "lastName": "Smith",
    "email": "bob@example.com",
    "phone": "555-987-6543"
  }' \
  | jq

# List contacts in tenant2
curl -X GET http://tenant2.localhost:8080/api/contacts \
  -H "Authorization: Bearer $tenant2_token" \
  | jq
```

#### Method B: Using X-Tenant-ID Header

Alternatively, you can use the tenant header approach:

```bash
# Create a contact in tenant1 using header
curl -X POST http://localhost:8080/api/contacts \
  -H "Content-Type: application/json" \
  -H "X-Tenant-ID: tenant1" \
  -H "Authorization: Bearer $tenant1_token" \
  -d '{
    "firstName": "Charlie",
    "lastName": "Brown",
    "email": "charlie@example.com",
    "phone": "555-555-5555"
  }' \
  | jq

# List contacts in tenant1 using header
curl -X GET http://localhost:8080/api/contacts \
  -H "X-Tenant-ID: tenant1" \
  -H "Authorization: Bearer $tenant1_token" \
  | jq
```

### 5. Testing Data Isolation

To verify tenant data isolation, try accessing tenant1's data with tenant2's credentials:

```bash
# This should fail or return empty results
curl -X GET http://tenant1.localhost:8080/api/contacts \
  -H "Authorization: Bearer $tenant2_token" \
  -v
```

Expected response should be a 403 Forbidden error or empty results list, demonstrating that tenant isolation is working correctly.

### 6. Browser-Based Testing

You can also test the multi-tenant functionality in a web browser:

1. Open your browser and navigate to:
   - http://tenant1.localhost:8081/swagger-ui.html

2. Use the Swagger UI to:
   - Authenticate with tenant1 credentials
   - Create and manage contacts for tenant1
   
3. Open a private/incognito window and navigate to:
   - http://tenant2.localhost:8081/swagger-ui.html
   
4. Authenticate with tenant2 credentials and verify you're working with a separate dataset

## Troubleshooting

### Common Issues

1. **Tenant Not Found**
   - Verify the tenant exists in the public.tenants table
   - Check that the tenant schema was created correctly
   - Ensure the tenant ID in the request matches the registered tenant

2. **Schema Not Found**
   - Verify Flyway migrations ran successfully
   - Check PostgreSQL logs for schema creation errors
   - Manually check if the schema exists in the database

3. **Subdomain Not Resolving**
   - Verify local DNS configuration
   - Try using the X-Tenant-ID header instead of subdomain
   - Check browser cache or try in incognito mode

4. **Service Discovery Issues**
   - Ensure Eureka server is running
   - Check that services are registered with Eureka
   - Verify network connectivity between services

### Diagnostic Commands

Check registered tenants:
```sql
SELECT * FROM public.tenants;
```

List all schemas:
```sql
SELECT schema_name FROM information_schema.schemata;
```

View tenant-specific tables:
```sql
SELECT table_name FROM information_schema.tables WHERE table_schema = 'tenant_tenant1';
```

### Helper Scripts

Create a `setup-local-env.sh` script to automate the local setup process:

```bash
#!/bin/bash
# SalesFlow local development environment setup

echo "Setting up SalesFlow multi-tenant environment..."

# 1. Configure hosts file
echo "Adding tenant entries to hosts file..."
sudo bash -c 'cat >> /etc/hosts << EOF
127.0.0.1   tenant1.localhost
127.0.0.1   tenant2.localhost
127.0.0.1   tenant3.localhost
127.0.0.1   salesflow.localhost
EOF'

# 2. Start infrastructure services
echo "Starting infrastructure services..."
cd SalesFlow-BE/authentication-service
docker-compose up -d postgres redis

# 3. Wait for services to be ready
echo "Waiting for services to initialize..."
sleep 10

# 4. Build all services
echo "Building services..."
cd ../
mvn clean install -DskipTests

echo "Setup complete! You can now start the services in separate terminals:"
echo "1. cd SalesFlow-BE/service-registry && mvn spring-boot:run"
echo "2. cd SalesFlow-BE/authentication-service && mvn spring-boot:run"
echo "3. cd SalesFlow-BE/contact-core-service && mvn spring-boot:run"
```

And a `register-tenants.sh` script to register test tenants:

```bash
#!/bin/bash
# Register test tenants and users

echo "Registering test tenants..."

# Register tenant1
curl -X POST http://localhost:8081/api/auth/tenant/register \
  -H "Content-Type: application/json" \
  -d '{"tenantId": "tenant1", "name": "Tenant 1 Organization"}'

# Register tenant2
curl -X POST http://localhost:8081/api/auth/tenant/register \
  -H "Content-Type: application/json" \
  -d '{"tenantId": "tenant2", "name": "Tenant 2 Organization"}'

echo "Creating test users..."

# Create user for tenant1
curl -X POST http://tenant1.localhost:8081/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "user1",
    "email": "user1@tenant1.com", 
    "password": "password123", 
    "tenantId": "tenant1",
    "firstName": "John",
    "lastName": "Doe"
  }'

# Create user for tenant2
curl -X POST http://tenant2.localhost:8081/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "user2", 
    "email": "user2@tenant2.com", 
    "password": "password123", 
    "tenantId": "tenant2",
    "firstName": "Jane",
    "lastName": "Smith"
  }'

echo "Tenant setup complete!"
```

## Advanced Multi-Tenant Concepts

### Tenant Provisioning Workflow

1. **Tenant Registration**
   - Admin creates new tenant via management interface
   - System validates tenant ID format and uniqueness
   - Tenant record is created in `public.tenants` table

2. **Schema Initialization**
   - Dedicated schema is created (`tenant_<tenant-id>`)
   - Flyway migrations apply table structures
   - Default data is seeded (lookup tables, settings)

3. **Resource Allocation**
   - Tenant-specific configurations are created
   - Storage quotas are assigned
   - Rate limits are configured

4. **Tenant Admin Setup**
   - Initial admin user is created
   - Welcome emails are sent
   - Onboarding process is initiated

### Tenant Data Migration

For moving data between tenants or during tenant restructuring:

```bash
# Export tenant data
pg_dump -h localhost -U postgres -d salesflow -n tenant_source --schema-only > schema.sql
pg_dump -h localhost -U postgres -d salesflow -n tenant_source --data-only > data.sql

# Modify schema references (if needed)
sed -i 's/tenant_source/tenant_destination/g' schema.sql
sed -i 's/tenant_source/tenant_destination/g' data.sql

# Import to destination tenant
psql -h localhost -U postgres -d salesflow -f schema.sql
psql -h localhost -U postgres -d salesflow -f data.sql
```

### Cross-Tenant Operations

In certain scenarios, operations may need to span multiple tenants:

1. **Aggregated Reporting**
   - Use database-level roles with cross-schema access permissions
   - Implement a dedicated reporting service with elevated privileges
   - Store aggregated data in a separate analytics schema

2. **Shared Resources**
   - Common reference data can be stored in a shared schema
   - Use database views to expose shared data to tenant schemas
   - Implement caching for frequently accessed shared data

### Dynamic Tenant Configuration

For tenant-specific customization:

```java
@Component
public class TenantConfigService {
    private final Map<String, TenantConfig> configCache = new ConcurrentHashMap<>();
    
    public TenantConfig getConfig(String tenantId) {
        return configCache.computeIfAbsent(tenantId, this::loadConfigFromDatabase);
    }
    
    private TenantConfig loadConfigFromDatabase(String tenantId) {
        // Load tenant-specific configuration from database
    }
}
```

## Security Considerations

### Tenant Isolation

1. **Data Isolation**
   - Enforce schema separation at database level
   - Use Row-Level Security (RLS) for additional protection
   - Audit schema access patterns regularly

2. **Connection Pooling**
   - Use separate connection pools per tenant
   - Implement connection labeling with tenant identifiers
   - Monitor connection usage for anomalies

3. **API Isolation**
   - Validate tenant context on every request
   - Implement tenant boundary checks in middleware
   - Use API gateways with tenant routing rules

### Authentication & Authorization

1. **JWT Token Security**
   - Include tenant information in JWT claims
   - Short expiration times (15-30 minutes)
   - Implement token refresh mechanism
   - Use strong signing keys (rotate regularly)

2. **Role-Based Access Control**
   - Implement tenant-specific roles
   - Use hierarchical permission structures
   - Include tenant context in authorization decisions

3. **OAuth2 Integration**
   - Configure tenant-aware OAuth2 resource servers
   - Map external identity providers to internal tenants
   - Support tenant-specific authentication providers

### Secure Configuration

1. **Tenant Secrets**
   - Store tenant API keys and secrets in a secure vault
   - Encrypt sensitive tenant configuration
   - Implement secure secret rotation procedures

2. **TLS Configuration**
   - Use wildcard certificates for tenant subdomains
   - Implement strict TLS settings (TLS 1.2+, strong ciphers)
   - Enable HSTS headers for all tenant domains

## Production Deployment

### Kubernetes Deployment

1. **Service Configuration**

```yaml
# Example Kubernetes deployment for authentication service
apiVersion: apps/v1
kind: Deployment
metadata:
  name: auth-service
spec:
  replicas: 3
  selector:
    matchLabels:
      app: auth-service
  template:
    metadata:
      labels:
        app: auth-service
    spec:
      containers:
      - name: auth-service
        image: salesflow/auth-service:latest
        ports:
        - containerPort: 8081
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "prod"
        - name: TENANT_BASE_DOMAIN
          value: "salesflow.com"
        - name: DB_URL
          valueFrom:
            secretKeyRef:
              name: db-credentials
              key: url
```

2. **Ingress Configuration**

```yaml
# Tenant subdomain routing with Kubernetes Ingress
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: tenant-routing
  annotations:
    kubernetes.io/ingress.class: nginx
    nginx.ingress.kubernetes.io/rewrite-target: /$2
spec:
  rules:
  - host: "*.salesflow.com"
    http:
      paths:
      - path: /api/auth(/|$)(.*)
        pathType: Prefix
        backend:
          service:
            name: auth-service
            port:
              number: 8081
      - path: /api/contacts(/|$)(.*)
        pathType: Prefix
        backend:
          service:
            name: contact-service
            port:
              number: 8080
```

### Database Scaling

1. **Connection Pooling**
   - Configure HikariCP with tenant-aware settings
   - Set appropriate pool sizes per tenant
   - Implement connection validation queries

2. **Database Partitioning**
   - For large deployments, consider database sharding
   - Implement tenant-to-shard mapping strategy
   - Use read replicas for high-read tenants

3. **Database Scaling Strategy**
```
# PostgreSQL connection pool configuration
spring.datasource.hikari.maximum-pool-size=50
spring.datasource.hikari.minimum-idle=10
spring.datasource.hikari.connection-timeout=30000
spring.datasource.hikari.idle-timeout=600000
spring.datasource.hikari.max-lifetime=1800000
```

### Cloud Deployment

1. **AWS Deployment**
   - Use Route53 for tenant subdomain DNS
   - Configure ALB with host-based routing
   - Use RDS for PostgreSQL with Multi-AZ

2. **Azure Deployment**
   - Use Azure DNS for tenant subdomain routing
   - Configure Application Gateway for tenant routing
   - Use Azure Database for PostgreSQL

3. **Google Cloud Deployment**
   - Use Cloud DNS for tenant subdomain mapping
   - Configure Cloud Load Balancing with host rules
   - Use Cloud SQL for PostgreSQL

## Performance Optimization

### Database Optimization

1. **Indexing Strategy**
   - Implement tenant-specific indices
   - Use partial indices for large tenants
   - Regularly analyze query performance

2. **Query Optimization**
   - Ensure tenant filter predicates are used efficiently
   - Implement query result caching
   - Use materialized views for complex reports

3. **Connection Management**
   - Set appropriate connection timeouts
   - Implement connection pooling optimized for multi-tenancy
   - Monitor connection usage patterns

### Caching Strategy

1. **Multi-Level Caching**
   - Implement tenant-aware application cache
   - Use Redis with tenant key prefixes
   - Consider tenant-specific cache expiration policies

2. **Cache Configuration**
```java
@Bean
public CacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
    // Create Redis cache manager with tenant-aware key prefix
    RedisCacheConfiguration cacheConfig = RedisCacheConfiguration.defaultCacheConfig()
        .computePrefixWith(cacheName -> {
            String tenantId = TenantContext.getCurrentTenant();
            return tenantId + "::" + cacheName + "::";
        });
    
    return RedisCacheManager.builder(redisConnectionFactory)
        .cacheDefaults(cacheConfig)
        .build();
}
```

### Resource Isolation

1. **CPU and Memory Limits**
   - Implement tenant resource quotas
   - Monitor resource usage per tenant
   - Set up alerts for tenant resource exhaustion

2. **Rate Limiting**
   - Implement tenant-specific API rate limits
   - Use token bucket algorithm for fair resource allocation
   - Configure graduated rate limits based on tenant tier

## Monitoring and Maintenance

### Tenant Monitoring

1. **Tenant-Specific Metrics**
   - Track API usage per tenant
   - Monitor database queries per tenant
   - Record error rates by tenant

2. **Logging Strategy**
   - Include tenant ID in all log messages
   - Implement log aggregation with tenant filtering
   - Set up tenant-specific log retention policies

3. **Prometheus Configuration**
```yaml
# Example Prometheus metric for tenant API usage
http_requests_total{tenant="tenant1",endpoint="/api/contacts"} 42
```

### Maintenance Operations

1. **Schema Migration**
   - Run Flyway migrations for all tenant schemas
   - Implement migration versioning and rollback
   - Schedule migrations during tenant low-usage periods

2. **Tenant Backup and Restore**
```bash
# Backup a specific tenant schema
pg_dump -h localhost -U postgres -d salesflow -n tenant_tenant1 > tenant1_backup.sql

# Restore a tenant schema
psql -h localhost -U postgres -d salesflow -f tenant1_backup.sql
```

3. **Tenant Deactivation and Deletion**
   - Implement soft deletion with deactivation period
   - Archive tenant data before deletion
   - Provide data export functionality for compliance

## Conclusion

### Summary

The SalesFlow multi-tenant architecture provides a robust solution for serving multiple customer organizations from a single application instance. By implementing schema-based multi-tenancy, the system achieves:

1. **Strong Data Isolation** - Each tenant's data is stored in a separate database schema, providing logical separation without the overhead of separate databases.

2. **Scalability** - The architecture allows for horizontal scaling of services while maintaining tenant isolation.

3. **Operational Efficiency** - Centralized deployment and management reduces operational costs compared to deploying separate instances for each tenant.

4. **Tenant Customization** - The design allows for tenant-specific configurations while sharing common code and infrastructure.

### Best Practices

When working with the SalesFlow multi-tenant system, follow these best practices:

1. **Always Test With Multiple Tenants** - Make sure new features work correctly across tenant boundaries.

2. **Use Tenant Context Throughout** - Ensure the tenant context is propagated through all service calls and database operations.

3. **Implement Proper Error Handling** - Add tenant-specific error handling to provide clear error messages without exposing other tenants' information.

4. **Monitor Tenant Resource Usage** - Set up monitoring to detect abnormal resource consumption by specific tenants.

5. **Regular Security Reviews** - Conduct periodic security reviews to ensure tenant isolation remains effective.

### Future Enhancements

The multi-tenant architecture can be extended in several ways:

1. **Tenant-Specific UI Customization** - Allow tenants to customize their UI experience.

2. **Tiered Service Levels** - Implement different service tiers with varying feature sets and resource allocations.

3. **Cross-Tenant Analytics** - Develop a secure way to analyze anonymized data across tenants for business intelligence.

4. **Tenant Migration Tools** - Create tools to easily migrate tenants between environments or upgrade tenant schemas.

5. **Self-Service Tenant Management** - Build a portal for tenants to manage their own settings and configurations.

By following this guide, you should be able to set up, run, and understand the SalesFlow multi-tenant architecture. The system is designed to be extensible, allowing you to add new features while maintaining the benefits of a multi-tenant approach.