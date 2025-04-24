# Tracebit Audit Logging API

A secure, scalable audit logging API for applications and services. Track user actions, system events, and security incidents with encrypted storage and flexible querying capabilities.

## Table of Contents
- [Overview](#overview)
- [Features](#features)
- [Prerequisites](#prerequisites)
- [Installation](#installation)
- [Configuration](#configuration)
- [API Documentation](#api-documentation)
- [Usage Examples](#usage-examples)
- [Troubleshooting](#troubleshooting)
- [Contributing](#contributing)
- [License](#license)
- [Contact](#contact)

## Overview

Tracebit is a comprehensive audit logging solution designed for modern applications and services. It provides a secure way to track and monitor user actions, system events, and security incidents with advanced features like encrypted storage, flexible querying, and alerting capabilities.

The API is built with Spring Boot and follows best practices for security, performance, and scalability.

## Features

### Security Enhancements

- **Improved Encryption**: Updated from basic AES to AES/GCM/NoPadding with proper initialization vectors
- **Externalized Secrets**: Moved encryption keys and API keys to configurable properties
- **API Key Management**: Added support for multiple API keys and key rotation
- **Rate Limiting**: Implemented rate limiting to prevent abuse

### Configuration Management

- **Externalized Configuration**: Added configurable properties for security settings
- **Environment Variables**: Support for environment variables for sensitive configuration
- **Data Retention**: Configurable data retention period

### API Enhancements

- **Pagination**: Added pagination support for audit log queries
- **Advanced Filtering**: Enhanced filtering capabilities by user ID, action, and date range
- **Detailed Responses**: Improved response structure with pagination metadata
- **New Endpoints**: Added endpoint to retrieve audit logs by ID

### Exception Handling

- **Global Exception Handler**: Implemented comprehensive exception handling
- **Consistent Responses**: Standardized error response format
- **Detailed Error Messages**: Improved error messages for better debugging
- **Security-Aware Errors**: Prevents leaking sensitive information in error messages

### Performance Optimizations

- **Query Optimization**: Added optimized queries for better database performance
- **Caching**: Implemented caching for frequently accessed audit logs
- **Efficient Filtering**: Improved database query efficiency with optimized repository methods

### Scalability Improvements

- **Asynchronous Processing**: Added async processing for audit log creation
- **Thread Pool Management**: Configured thread pools for background tasks
- **Horizontal Scaling Support**: Prepared for deployment in clustered environments

### Data Management

- **Data Retention Policy**: Implemented scheduled purging of old audit logs
- **Automatic Cleanup**: Configured scheduled tasks for maintenance
- **Data Export**: Support for exporting audit logs in various formats (CSV, PDF)

### Alert Rules

- **Customizable Alerts**: Create and manage alert rules for specific conditions
- **Notification System**: Get notified when important audit events occur
- **Startup-specific Rules**: Configure different alert rules for different startups

## Prerequisites

- Java 17 or higher
- PostgreSQL 12 or higher
- Maven 3.6 or higher

## Installation

1. Clone the repository:
   ```
   git clone https://github.com/yourusername/tracebit.git
   cd tracebit
   ```

2. Configure environment variables:
   ```
   export TRACEBIT_ENCRYPTION_KEY=your-secure-encryption-key
   export TRACEBIT_API_KEYS=key1,key2,key3
   ```

3. Configure the database in `application.properties` or via environment variables:
   ```
   spring.datasource.url=jdbc:postgresql://localhost:5432/tracebit
   spring.datasource.username=your-username
   spring.datasource.password=your-password
   ```

4. Build the application:
   ```
   ./mvnw clean package
   ```

5. Run the application:
   ```
   ./mvnw spring-boot:run
   ```

6. Access the API documentation:
   ```
   http://localhost:8686/swagger-ui.html
   ```

## Configuration Properties

| Property | Description | Default |
|----------|-------------|---------|
| `tracebit.encryption.key` | Encryption key for sensitive data | Environment variable |
| `tracebit.api.keys` | Comma-separated list of valid API keys | Environment variable |
| `tracebit.api.rate-limit` | Maximum requests per time window | 100 |
| `tracebit.api.rate-limit-reset-ms` | Rate limit reset period in milliseconds | 60000 |
| `tracebit.data.retention-days` | Number of days to retain audit logs | 90 |

## API Documentation

### Audit Log Endpoints

- `POST /api/v1/logs` - Create a new audit log
- `GET /api/v1/logs` - Search audit logs with filtering and pagination
- `GET /api/v1/logs/{id}` - Get a specific audit log by ID
- `GET /api/v1/logs/export` - Export audit logs in CSV or PDF format

### Alert Rule Endpoints

- `POST /api/v1/alert-rules` - Create a new alert rule
- `PUT /api/v1/alert-rules/{id}` - Update an existing alert rule
- `DELETE /api/v1/alert-rules/{id}` - Delete an alert rule
- `GET /api/v1/alert-rules/{id}` - Get a specific alert rule by ID
- `GET /api/v1/alert-rules/startup/{startupId}` - Get all alert rules for a specific startup

### Authentication

All API endpoints require authentication using the `X-TRACEBIT-KEY` header with a valid API key.

Example:
```
X-TRACEBIT-KEY: your-api-key
```

## Usage Examples

### Creating an Audit Log

```bash
curl -X POST http://localhost:8686/api/v1/logs \
  -H "Content-Type: application/json" \
  -H "X-TRACEBIT-KEY: your-api-key" \
  -d '{
     "userId": "akins@tracebit.dev",
     "action": "login",
     "target": "admin_account",
     "meta": {
       "ip": "15.12.45.22",
       "device": "Mac OS - Chrome 121",
       "location": "Lagos, Nigeria"
        }
    }'
```

### Searching Audit Logs

```bash
curl -X GET "http://localhost:8686/api/v1/logs?userId=user123&action=LOGIN&page=0&size=20" \
  -H "X-TRACEBIT-KEY: your-api-key"
```

### Creating an Alert Rule

```bash
curl -X POST http://localhost:8686/api/v1/alert-rules \
  -H "Content-Type: application/json" \
  -H "X-TRACEBIT-KEY: your-api-key" \
  -d '{
     "name": "Failed Login Alerts",
     "description": "Triggers when login failures are detected",
     "startupId": "startup123",
     "matchType": "EXACT",
     "field": "action",
     "pattern": "LOGIN_FAILED",
     "callbackUrl": "https://example.com/webhook",
     "secretToken": "your-secret-token",
     "active": true
    }'
```

## Troubleshooting

### Common Issues

1. **Database Connection Issues**
   - Verify PostgreSQL is running
   - Check database credentials in application.properties
   - Ensure the database exists and is accessible

2. **API Key Authentication Failures**
   - Verify the API key is correctly set in the X-TRACEBIT-KEY header
   - Check that the API key is included in the TRACEBIT_API_KEYS environment variable

3. **Encryption Errors**
   - Ensure TRACEBIT_ENCRYPTION_KEY is set and is at least 16 characters long
   - Check for encryption-related errors in the application logs

### Logging

To enable debug logging, add the following to your application.properties:

```
logging.level.dev.io.tracebit=DEBUG
```

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.

## Contact

Tracebit Support - [akintomiwa.abolade@gmail.com](mailto:akintomiwa.abolade@gmail.com)

Project Link: [https://github.com/akintomiwa-abolade/tracebit](https://github.com/yourusername/tracebit)
