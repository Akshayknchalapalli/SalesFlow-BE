# Authentication Service

This service handles user authentication and authorization for the SalesFlow application. It provides JWT-based authentication with role-based access control and schema-based multi-tenant isolation.

## Features

- User registration and login
- JWT token-based authentication
- Refresh token mechanism
- Role-based access control
- Schema-based multi-tenant isolation
- UUID/GUID-based entity identifiers
- Integration with other microservices

## Prerequisites

- Java 17 or higher
- PostgreSQL 12 or higher
- Maven 3.6 or higher

## Setup

1. Create a `.env` file from the provided `.env.example`:
```bash
cp .env.example .env
```

2. Update the values in the `.env` file with your specific configuration:
```properties
# Database Configuration
POSTGRES_USER=your_postgres_username
POSTGRES_PASSWORD=your_postgres_password
POSTGRES_DB=salesflowdb-dev

# JWT Configuration
JWT_SECRET_KEY=your-256-bit-secret-key-here

# Email Configuration
MAIL_USERNAME=your_email@gmail.com
MAIL_PASSWORD=your_app_password
```

3. Build the application:
```bash
mvn clean install
```

4. Run the application:
```bash
mvn spring-boot:run
```

## API Endpoints

### Authentication

#### Register User
```http
POST /api/auth/register
Content-Type: application/json

{
    "username": "string",
    "email": "string",
    "password": "string",
    "tenantId": "UUID string"
}
```

#### Login
```http
POST /api/auth/login
Content-Type: application/json

{
    "username": "string",
    "password": "string"
}
```

#### Refresh Token
```http
POST /api/auth/refresh
Content-Type: application/json

"refresh_token_string"
```

## Response Format

### Success Response
```json
{
    "accessToken": "string",
    "refreshToken": "string",
    "username": "string",
    "email": "string",
    "tenantId": "UUID string",
    "tenantName": "string",
    "roles": ["string"]
}
```

### Error Response
```json
{
    "status": number,
    "error": "string",
    "message": "string"
}
```

## Security

- All endpoints except `/api/auth/**` require authentication
- JWT tokens are used for authentication
- Passwords are encrypted using BCrypt
- Role-based access control is implemented
- Schema-based tenant isolation is enforced
- Each tenant has a dedicated database schema named `tenant_<tenantName>`

### Environment Variables

The application uses the following key environment variables:

| Variable | Description | Example |
|----------|-------------|---------|
| JWT_SECRET_KEY | Secret key for JWT token signing | `a-very-secure-secret-key-with-sufficient-entropy` |
| ACCESS_TOKEN_VALIDITY | JWT access token validity in minutes | 30 |
| REFRESH_TOKEN_VALIDITY | JWT refresh token validity in days | 7 |
| DB_URL | Database connection URL | jdbc:postgresql://localhost:5432/salesflowdb-dev |
| MAIL_HOST | SMTP server host | smtp.gmail.com |
| TWILIO_ACCOUNT_SID | Twilio account SID for SMS | AC1234567890abcdef |

## Integration with Other Services

The authentication service integrates with other microservices through JWT tokens. Each token contains:
- User information
- Role information
- Tenant information (ID and name)

Other services should validate the JWT token and extract the tenant information for proper data isolation.

## Multi-Tenant Implementation

The service implements schema-based multi-tenancy where:

- Each tenant has its own isolated database schema named `tenant_<tenantName>`
- Tenant identification occurs via subdomain (e.g., `acme.salesflow.com`) or X-Tenant-ID header
- All entities use UUID/GUID identifiers for better security and portability
- Tenant data is completely isolated at the database level
- Flyway manages schema migrations for both shared and tenant-specific schemas

See [MULTI-TENANT-IMPLEMENTATION.md](MULTI-TENANT-IMPLEMENTATION.md) for detailed information.

## Development

### Adding New Roles

1. Add the role to the auth schema migration script
2. Update the security configuration if needed
3. Assign the role to users as required

### Customizing Tenant Validation

Modify the `SubdomainTenantResolver` class to implement your specific tenant resolution rules.

### Creating New Tenants

Use the Tenant API to create new tenants:

```http
POST /api/tenants
Content-Type: application/json

{
    "name": "Acme Corporation",
    "adminEmail": "admin@acme.com",
    "adminPassword": "securePassword"
}
```

This will:
1. Create a new tenant record with a UUID
2. Create a tenant-specific schema named `tenant_acme_corporation`
3. Run tenant-specific migrations
4. Create an admin user for the tenant

## Development

### Prerequisites

- Java 17 or higher
- PostgreSQL 12 or higher
- Maven 3.6 or higher
- Docker (optional, for containerized development)

### Docker Development

To run the service with Docker:

```bash
# Build and start containers
docker-compose up -d

# View logs
docker-compose logs -f auth-service
```

### Testing

Run tests with:
```bash
mvn test
```

## Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request 