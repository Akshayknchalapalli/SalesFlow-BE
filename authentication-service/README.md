# Authentication Service

This service handles user authentication and authorization for the SalesFlow application. It provides JWT-based authentication with role-based access control and tenant-based isolation.

## Features

- User registration and login
- JWT token-based authentication
- Refresh token mechanism
- Role-based access control
- Tenant-based access control
- Integration with other microservices

## Prerequisites

- Java 17 or higher
- PostgreSQL 12 or higher
- Maven 3.6 or higher

## Setup

1. Configure the database in `application.properties`:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/auth_service
spring.datasource.username=your_username
spring.datasource.password=your_password
```

2. Set the JWT secret key in environment variables:
```bash
export JWT_SECRET_KEY=your-256-bit-secret-key-here
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
    "tenantId": "string"
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
    "tenantId": "string",
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
- Tenant-based isolation is enforced

## Integration with Other Services

The authentication service integrates with other microservices through JWT tokens. Each token contains:
- User information
- Role information
- Tenant information

Other services should validate the JWT token and extract the tenant ID for proper data isolation.

## Development

### Adding New Roles

1. Add the role to `data.sql`
2. Update the security configuration if needed
3. Assign the role to users as required

### Customizing Tenant Validation

Modify the `TenantValidator` class to implement your specific tenant validation rules.

## Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request 