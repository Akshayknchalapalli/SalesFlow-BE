# Contact Core Service

## Overview
The Contact Core Service is a microservice responsible for managing contact information within the SalesFlow system. It provides APIs for creating, updating, deleting, and retrieving contact details.

## Features
- Create, update, and delete contacts
- Retrieve contact details by ID
- List all contacts with pagination
- Search contacts by various criteria
- Manage contact stages and tags

## Installation
1. Clone the repository:
   ```bash
   git clone <repository-url>
   cd contact-core-service
   ```
2. Build the project using Maven:
   ```bash
   mvn clean install
   ```
3. Run the application:
   ```bash
   mvn spring-boot:run
   ```

## Usage
### API Endpoints
- **POST /api/contacts**: Create a new contact
- **GET /api/contacts/{id}**: Retrieve a contact by ID
- **PUT /api/contacts/{id}**: Update a contact
- **DELETE /api/contacts/{id}**: Delete a contact
- **GET /api/contacts**: List all contacts (paginated)
- **GET /api/contacts/search**: Search contacts
- **GET /api/contacts/stage/{stage}**: Get contacts by stage
- **GET /api/contacts/stage/{stage}/count**: Count contacts by stage
- **GET /api/contacts/exists**: Check if a contact exists by email
- **POST /api/contacts/bulk**: Create multiple contacts

## Dependencies
- Spring Boot
- Spring Data JPA
- Spring Security
- Lombok
- MapStruct
- Redis (for caching)
- Kafka (for event handling)

## License
This project is licensed under the Apache License 2.0. See the LICENSE file for details.

## Environment Setup

### Setting up Environment Variables

For local development, create a `.env` file in the root of the `contact-core-service` directory with the following variables:

```
# Database Configuration (Required)
SUPABASE_DATABASE_URL=jdbc:postgresql://db.rdczmzmusjfemlynaqdf.supabase.co:5432/postgres
DATABASE_USERNAME=postgres
DATABASE_PASSWORD=your_password_here

# No need to specify AUTH_SERVICE_URL in the .env file
# It's already configured with a default value of http://localhost:8081
```

Replace the placeholder values with your actual credentials.

**Note:** 
1. The `.env` file is included in `.gitignore` and should never be committed to version control.
2. Only the database configuration variables are required in the `.env` file.
3. The `AUTH_SERVICE_URL` is automatically set to `http://localhost:8081` in the launch configuration. 