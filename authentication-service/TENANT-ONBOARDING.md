# SalesFlow CRM: Tenant Registration and Onboarding Documentation

## Table of Contents

1. [Overview](#overview)
2. [System Architecture](#system-architecture)
3. [Tenant Registration Process](#tenant-registration-process)
4. [API Reference](#api-reference)
5. [Frontend Integration Guide](#frontend-integration-guide)
6. [Authentication Flow](#authentication-flow)
7. [Multi-Tenant URL Structure](#multi-tenant-url-structure)
8. [User Management Within Tenants](#user-management-within-tenants)
9. [Error Handling](#error-handling)
10. [Security Considerations](#security-considerations)
11. [Troubleshooting](#troubleshooting)

## Overview

The SalesFlow CRM is a multi-tenant system where each tenant represents a separate organization with its own isolated data and users. This document outlines the complete process for registering and onboarding new tenants, including the necessary API calls, authentication flows, and frontend integration guidance.

### Key Terminology

- **System Administrator**: Users with `ROLE_ADMIN` who can create and manage tenants
- **Tenant**: An organization using the SalesFlow CRM platform with its own isolated data
- **Tenant Administrator**: Users with `ROLE_TENANT_ADMIN` who manage a specific tenant
- **Regular User**: Users with `ROLE_USER` who operate within a specific tenant

## System Architecture

### Multi-Tenant Implementation

SalesFlow uses a schema-based multi-tenancy approach:

- Each tenant has a separate database schema
- The tenant registry is stored in the public schema
- Authentication is handled centrally but maintains tenant isolation
- API endpoints enforce tenant context for data access

### URL Structure

- System-level access: `http://localhost:8081`
- Tenant-specific access: `http://[tenant-name].localhost:8081` (development)
- Production: `https://[tenant-name].salesflow.com`

## Tenant Registration Process

### 1. System Administrator Login

**Endpoint**: `POST /api/auth/login`

**Request**:
```json
{
  "username": "admin",
  "password": "admin123"
}
```

**Response**:
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1...",
    "refreshToken": "eyJhbGciOiJIUzI1...",
    "username": "admin",
    "email": "admin@system.com",
    "tenantId": "00000000-0000-0000-0000-000000000001",
    "roles": ["ROLE_ADMIN"]
  }
}
```

### 2. Create New Tenant

**Endpoint**: `POST /api/admin/tenants`

**Headers**:
```
Authorization: Bearer eyJhbGciOiJIUzI1...
```

**Request**:
```json
{
  "name": "NewCompany",
  "adminEmail": "admin@newcompany.com",
  "adminPassword": "securePassword123"
}
```

**Response**:
```json
{
  "success": true,
  "message": "Tenant created successfully",
  "data": {
    "tenantId": "550e8400-e29b-41d4-a716-446655440000",
    "name": "NewCompany",
    "domain": "newcompany.salesflow.com",
    "active": true,
    "plan": "standard",
    "createdAt": "2023-09-15T14:30:45"
  }
}
```

**Important Notes**:
- The tenant name must be unique and follow the pattern: `[a-zA-Z0-9-_]+`
- The system automatically creates a tenant admin user with username: `[tenant-name]-admin`
- The tenant UUID is generated automatically unless explicitly provided

### 3. Tenant Administrator Login

**Endpoint**: `POST /api/auth/login`

**URL**: `http://newcompany.localhost:8081/api/auth/login` (Development)  
**Production URL**: `https://newcompany.salesflow.com/api/auth/login`

**Request**:
```json
{
  "username": "NewCompany-admin",
  "password": "securePassword123"
}
```

**Response**:
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1...",
    "refreshToken": "eyJhbGciOiJIUzI1...",
    "username": "NewCompany-admin",
    "email": "admin@newcompany.com",
    "tenantId": "550e8400-e29b-41d4-a716-446655440000",
    "roles": ["ROLE_TENANT_ADMIN", "ROLE_USER"]
  }
}
```

### 4. Tenant Administrator Creates Users

**Endpoint**: `POST /api/auth/register`

**URL**: `http://newcompany.localhost:8081/api/auth/register`

**Headers**:
```
Authorization: Bearer eyJhbGciOiJIUzI1...
```

**Request**:
```json
{
  "username": "john.doe",
  "email": "john.doe@newcompany.com",
  "password": "userPassword123",
  "confirmPassword": "userPassword123",
  "tenantId": "550e8400-e29b-41d4-a716-446655440000",
  "requestedRole": "USER"
}
```

**Response**:
```json
{
  "success": true,
  "message": "User registered successfully",
  "data": {
    "username": "john.doe",
    "email": "john.doe@newcompany.com",
    "tenantId": "550e8400-e29b-41d4-a716-446655440000",
    "roles": ["ROLE_USER"]
  }
}
```

**Important Notes**:
- The `tenantId` must match the tenant UUID for the current tenant
- Available roles for new users are: `USER` and `TENANT_ADMIN`
- Only users with `ROLE_TENANT_ADMIN` can create users with `ROLE_TENANT_ADMIN`

### 5. User Login to Tenant

**Endpoint**: `POST /api/auth/login`

**URL**: `http://newcompany.localhost:8081/api/auth/login`

**Request**:
```json
{
  "username": "john.doe",
  "password": "userPassword123"
}
```

**Response**:
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1...",
    "refreshToken": "eyJhbGciOiJIUzI1...",
    "username": "john.doe",
    "email": "john.doe@newcompany.com",
    "tenantId": "550e8400-e29b-41d4-a716-446655440000",
    "roles": ["ROLE_USER"]
  }
}
```

## API Reference

### System Administrator Endpoints

| Endpoint | Method | Description | Required Role | Request Body | Response |
|----------|--------|-------------|--------------|--------------|----------|
| `/api/auth/login` | POST | System admin login | None | Username/password | Access and refresh tokens |
| `/api/admin/tenants` | GET | List all tenants | ADMIN | None | List of all tenants |
| `/api/admin/tenants` | POST | Create new tenant | ADMIN | Tenant details | New tenant information |
| `/api/admin/tenants/{tenantId}` | GET | Get tenant by ID | ADMIN | None | Tenant details |
| `/api/admin/tenants/{tenantId}/deactivate` | PUT | Deactivate tenant | ADMIN | None | Success message |
| `/api/admin/tenants/{tenantId}/reactivate` | PUT | Reactivate tenant | ADMIN | None | Success message |
| `/api/admin/tenants/{tenantId}/statistics` | GET | Get tenant statistics | ADMIN | None | Tenant usage statistics |
| `/api/admin/tenants/check-availability` | GET | Check tenant ID availability | ADMIN | Query param: tenantId | Availability status |

### Tenant Administration Endpoints

| Endpoint | Method | Description | Required Role | Request Body | Response |
|----------|--------|-------------|--------------|--------------|----------|
| `/api/auth/login` | POST | Tenant admin login | None | Username/password | Access and refresh tokens |
| `/api/tenants/info` | GET | Get current tenant info | TENANT_ADMIN, USER | None | Tenant details |
| `/api/tenants/users` | GET | Get all users in tenant | TENANT_ADMIN | None | List of tenant users |
| `/api/auth/register` | POST | Create new user | TENANT_ADMIN | User details | New user information |

### User Authentication Endpoints

| Endpoint | Method | Description | Required Role | Request Body | Response |
|----------|--------|-------------|--------------|--------------|----------|
| `/api/auth/login` | POST | User login | None | Username/password | Access and refresh tokens |
| `/api/auth/refresh` | POST | Refresh tokens | None | Refresh token | New access and refresh tokens |
| `/api/auth/validate` | GET | Validate token | Any | None (token in header) | User information |
| `/api/auth/forgot-password` | POST | Request password reset | None | Email | Success message |
| `/api/auth/reset-password` | POST | Reset password | None | Token, new password | Success message |

## Frontend Integration Guide

### 1. Registration Form for Tenants

When creating a registration form for new tenants, collect:

- Tenant name (will be used for subdomain)
- Admin email
- Admin password (with confirmation)
- Optional plan selection

Example React component:
```jsx
function TenantRegistrationForm() {
  const [formData, setFormData] = useState({
    name: '',
    adminEmail: '',
    adminPassword: '',
    confirmPassword: ''
  });
  
  const handleSubmit = async (e) => {
    e.preventDefault();
    
    // Validate form data
    if (formData.adminPassword !== formData.confirmPassword) {
      alert('Passwords do not match');
      return;
    }
    
    try {
      // First login as admin
      const loginResponse = await fetch('http://localhost:8081/api/auth/login', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({
          username: 'admin',
          password: 'admin123'
        })
      });
      
      const loginData = await loginResponse.json();
      
      // Then create tenant
      const response = await fetch('http://localhost:8081/api/admin/tenants', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${loginData.data.accessToken}`
        },
        body: JSON.stringify({
          name: formData.name,
          adminEmail: formData.adminEmail,
          adminPassword: formData.adminPassword
        })
      });
      
      const result = await response.json();
      
      if (result.success) {
        // Redirect to tenant login page
        window.location.href = `http://${formData.name}.localhost:8081/login`;
      } else {
        alert(`Error: ${result.message}`);
      }
    } catch (error) {
      console.error('Error creating tenant:', error);
      alert('Failed to create tenant. Please try again.');
    }
  };
  
  // Form rendering code...
}
```

### 2. Tenant Login Page

Create a tenant-specific login page that detects the subdomain:

```jsx
function TenantLoginPage() {
  const [credentials, setCredentials] = useState({
    username: '',
    password: ''
  });
  
  // Extract tenant from subdomain
  const subdomain = window.location.hostname.split('.')[0];
  const isTenantSubdomain = subdomain !== 'localhost' && subdomain !== 'salesflow';
  
  const handleLogin = async (e) => {
    e.preventDefault();
    
    try {
      const response = await fetch('/api/auth/login', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(credentials)
      });
      
      const result = await response.json();
      
      if (result.success) {
        // Store tokens in localStorage
        localStorage.setItem('accessToken', result.data.accessToken);
        localStorage.setItem('refreshToken', result.data.refreshToken);
        localStorage.setItem('userInfo', JSON.stringify({
          username: result.data.username,
          email: result.data.email,
          tenantId: result.data.tenantId,
          roles: result.data.roles
        }));
        
        // Redirect to dashboard
        window.location.href = '/dashboard';
      } else {
        alert(`Login failed: ${result.message}`);
      }
    } catch (error) {
      console.error('Login error:', error);
      alert('Login failed. Please try again.');
    }
  };
  
  // Form rendering code...
}
```

### 3. Token Management in Frontend

Implement a utility for handling tokens and authentication:

```javascript
// auth.js
const AUTH_STORAGE_KEY = 'salesflow_auth';

export const saveAuthData = (authData) => {
  localStorage.setItem(AUTH_STORAGE_KEY, JSON.stringify({
    accessToken: authData.accessToken,
    refreshToken: authData.refreshToken,
    expiresAt: Date.now() + (3600 * 1000), // Assuming 1 hour expiry
    userInfo: {
      username: authData.username,
      email: authData.email,
      tenantId: authData.tenantId,
      roles: authData.roles
    }
  }));
};

export const getAuthData = () => {
  try {
    return JSON.parse(localStorage.getItem(AUTH_STORAGE_KEY));
  } catch (e) {
    return null;
  }
};

export const clearAuthData = () => {
  localStorage.removeItem(AUTH_STORAGE_KEY);
};

export const getAccessToken = async () => {
  const authData = getAuthData();
  
  if (!authData) {
    return null;
  }
  
  // Check if token is expired
  if (Date.now() >= authData.expiresAt) {
    return refreshTokens(authData.refreshToken);
  }
  
  return authData.accessToken;
};

export const refreshTokens = async (refreshToken) => {
  try {
    const response = await fetch('/api/auth/refresh', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        refreshToken
      })
    });
    
    const result = await response.json();
    
    if (result.success) {
      saveAuthData(result.data);
      return result.data.accessToken;
    } else {
      clearAuthData();
      window.location.href = '/login';
      return null;
    }
  } catch (error) {
    console.error('Token refresh failed:', error);
    clearAuthData();
    window.location.href = '/login';
    return null;
  }
};

export const isAdmin = () => {
  const authData = getAuthData();
  return authData?.userInfo?.roles.includes('ROLE_ADMIN');
};

export const isTenantAdmin = () => {
  const authData = getAuthData();
  return authData?.userInfo?.roles.includes('ROLE_TENANT_ADMIN');
};
```

### 4. Authenticated API Requests

Create a utility for making authenticated API requests:

```javascript
// api.js
import { getAccessToken } from './auth';

export const apiRequest = async (endpoint, options = {}) => {
  const accessToken = await getAccessToken();
  
  if (!accessToken) {
    throw new Error('Not authenticated');
  }
  
  const headers = {
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${accessToken}`,
    ...options.headers
  };
  
  const response = await fetch(endpoint, {
    ...options,
    headers
  });
  
  const result = await response.json();
  
  if (!response.ok) {
    throw new Error(result.message || 'API request failed');
  }
  
  return result;
};
```

### 5. User Registration Form

Create a form for tenant administrators to register new users:

```jsx
function UserRegistrationForm() {
  const [formData, setFormData] = useState({
    username: '',
    email: '',
    password: '',
    confirmPassword: '',
    requestedRole: 'USER'
  });
  
  // Get tenant ID from stored user info
  const userInfo = JSON.parse(localStorage.getItem('userInfo'));
  const tenantId = userInfo.tenantId;
  
  const handleSubmit = async (e) => {
    e.preventDefault();
    
    // Validate form data
    if (formData.password !== formData.confirmPassword) {
      alert('Passwords do not match');
      return;
    }
    
    try {
      const response = await apiRequest('/api/auth/register', {
        method: 'POST',
        body: JSON.stringify({
          ...formData,
          tenantId
        })
      });
      
      if (response.success) {
        alert('User registered successfully');
        // Clear form or redirect
      }
    } catch (error) {
      console.error('Registration error:', error);
      alert(`Registration failed: ${error.message}`);
    }
  };
  
  // Form rendering code...
}
```

## Authentication Flow

### 1. Initial Login Flow

```
User → Frontend: Enter credentials
Frontend → AuthService: POST /api/auth/login
AuthService → Database: Validate credentials
Database → AuthService: User data
AuthService → AuthService: Generate JWT tokens
AuthService → Frontend: Return tokens & user info
Frontend → Frontend: Store tokens
Frontend → User: Redirect to dashboard
```

### 2. Token Refresh Flow

```
Frontend → Frontend: Detect expired token
Frontend → AuthService: POST /api/auth/refresh
AuthService → Database: Validate refresh token
Database → AuthService: Token status
AuthService → AuthService: Generate new tokens
AuthService → Frontend: Return new tokens
Frontend → Frontend: Update stored tokens
```

### 3. User Registration Flow

```
TenantAdmin → Frontend: Complete registration form
Frontend → AuthService: POST /api/auth/register
AuthService → Database: Check username/email uniqueness
Database → AuthService: Validation result
AuthService → Database: Create new user
AuthService → Frontend: Return success/failure
Frontend → TenantAdmin: Display result
```

## Multi-Tenant URL Structure

### Development Environment

- System Administration: `http://localhost:8081`
- Tenant Access: `http://[tenant-name].localhost:8081`

### Production Environment

- System Administration: `https://admin.salesflow.com`
- Tenant Access: `https://[tenant-name].salesflow.com`

### API Endpoints

- System API: `http://localhost:8081/api/...`
- Tenant API: `http://[tenant-name].localhost:8081/api/...`

## User Management Within Tenants

### User Roles

1. **ROLE_ADMIN** (System Administrator)
   - Can create and manage tenants
   - Can access system-wide settings
   - Cannot access tenant-specific data directly

2. **ROLE_TENANT_ADMIN** (Tenant Administrator)
   - Can manage users within their tenant
   - Can configure tenant-specific settings
   - Cannot access other tenants

3. **ROLE_USER** (Regular User)
   - Can access tenant-specific features
   - Limited to their assigned permissions
   - Cannot manage other users

### User Registration Process

1. **First Tenant Admin**:
   - Created automatically during tenant creation
   - Username format: `[tenant-name]-admin`

2. **Additional Users**:
   - Created by tenant admin using the `/api/auth/register` endpoint
   - Must specify the correct tenant ID
   - Can be assigned USER or TENANT_ADMIN roles

### User Listing and Management

Tenant administrators can:
- List all users in their tenant: `GET /api/tenants/users`
- Create new users: `POST /api/auth/register`
- Future endpoints will allow disabling users and role management

## Error Handling

### Common Error Responses

```json
{
  "success": false,
  "message": "Error message description",
  "data": null
}
```

### HTTP Status Codes

- **200 OK**: Request succeeded
- **400 Bad Request**: Invalid input
- **401 Unauthorized**: Invalid credentials or token
- **403 Forbidden**: Insufficient permissions
- **404 Not Found**: Resource not found
- **500 Internal Server Error**: Server-side error

### Frontend Error Handling

```javascript
try {
  const response = await apiRequest('/api/endpoint', options);
  // Handle success
} catch (error) {
  // Display appropriate error message
  if (error.status === 401) {
    // Redirect to login
  } else if (error.status === 403) {
    // Display permission error
  } else {
    // Display generic error
  }
}
```

## Security Considerations

### 1. Token Storage

- Store tokens in browser storage (localStorage or sessionStorage)
- For enhanced security, consider using HttpOnly cookies

```javascript
// Secure token storage with expiry
const storeTokenSecurely = (tokens) => {
  const tokenData = {
    accessToken: tokens.accessToken,
    refreshToken: tokens.refreshToken,
    expiresAt: Date.now() + (3600 * 1000)
  };
  
  localStorage.setItem('auth_tokens', JSON.stringify(tokenData));
};
```

### 2. CSRF Protection

- Include CSRF tokens in forms
- SalesFlow backend has CSRF protection built-in

### 3. XSS Prevention

- Sanitize all user inputs
- Use React's built-in XSS protection
- Avoid using `dangerouslySetInnerHTML`

### 4. Tenant Isolation

- Always include tenant context in requests
- Never expose tenant IDs of other tenants
- Validate tenant access on both frontend and backend

## Troubleshooting

### Common Issues and Solutions

1. **"Invalid tenant ID" error**:
   - Ensure the correct tenant ID is being passed in the registration request
   - Verify the tenant exists and is active

2. **"Username already exists" error**:
   - Usernames must be unique across the entire system, not just within a tenant
   - Choose a different username

3. **"Tenant name already exists" error**:
   - Tenant names must be unique
   - Choose a different tenant name

4. **Cannot access tenant subdomain**:
   - In development, ensure you've configured your hosts file
   - Add entries like `127.0.0.1 tenant1.localhost`

5. **Token validation failures**:
   - Check that the token hasn't expired
   - Ensure you're including the "Bearer " prefix
   - Verify you're using the correct token type (access vs. refresh)

### Debugging Tenant Context

Use the debug endpoint to check the current tenant context:

```javascript
const checkTenantContext = async () => {
  try {
    const response = await apiRequest('/api/debug/tenant/context');
    console.log('Tenant context:', response.data);
  } catch (error) {
    console.error('Failed to get tenant context:', error);
  }
};
```

---

This documentation provides a comprehensive guide to the tenant registration and onboarding process in the SalesFlow CRM system. It covers all the necessary API endpoints, authentication flows, and frontend integration guidance needed to implement a complete multi-tenant solution.

For any additional questions or updates to this documentation, please contact the SalesFlow development team.