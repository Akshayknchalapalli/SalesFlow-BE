# SalesFlow Contact Management System

A comprehensive contact management system built with Spring Boot microservices architecture.

## System Architecture

The system is built using a microservices architecture with the following components:

1. Contact Core Service
2. Contact Tag Service
3. Document Management Service
4. Interaction Service
5. Integration Gateway
6. Analytics Service

## Prerequisites

- Java 17 or higher
- Maven 3.8 or higher
- PostgreSQL 14 or higher
- Redis 6 or higher
- Docker and Docker Compose (for local development)

## Getting Started

### Local Development Setup

1. Clone the repository:
```bash
git clone https://github.com/your-org/salesflow.git
cd salesflow
```

2. Start the required services using Docker Compose:
```bash
docker-compose up -d postgres redis
```

3. Build the project:
```bash
mvn clean install
```

4. Run the services:
```bash
mvn spring-boot:run -pl contact-core-service
```

### Configuration

The services can be configured using environment variables or application.yml files. Key configurations include:

- Database connection details
- Redis connection details
- Service discovery settings
- Security configurations

## API Documentation

Once the services are running, you can access the API documentation at:

- Swagger UI: http://localhost:8081/api/swagger-ui.html
- OpenAPI Spec: http://localhost:8081/api/api-docs

## Security

The system uses OAuth2/OIDC for authentication and authorization. Key security features include:

- JWT-based authentication
- Role-based access control
- API rate limiting
- Input validation
- CORS configuration

## Development Guidelines

### Code Style

- Follow Google Java Style Guide
- Use meaningful variable and method names
- Write comprehensive unit tests
- Document public APIs

### Git Workflow

1. Create feature branches from develop
2. Write meaningful commit messages
3. Create pull requests for code review
4. Merge to develop after approval

### Testing

- Write unit tests for all business logic
- Include integration tests for API endpoints
- Use test containers for database tests
- Maintain good test coverage

## Deployment

The services can be deployed using:

- Docker containers
- Kubernetes
- Cloud platforms (AWS, GCP, Azure)

## Monitoring

The system includes:

- Health check endpoints
- Prometheus metrics
- Distributed tracing
- Centralized logging

## Contributing

1. Fork the repository
2. Create your feature branch
3. Commit your changes
4. Push to the branch
5. Create a pull request

## License

This project is licensed under the MIT License - see the LICENSE file for details. 