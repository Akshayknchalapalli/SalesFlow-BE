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