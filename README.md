# Rafiq Backend - Healthcare Application

A Spring Boot-based healthcare application built with a **modular monolith architecture** that provides comprehensive medical services including medication management with reminders, lab test analysis, doctor and patient management, and AI-powered medical document analysis.

## Table of Contents
- [Architecture](#architecture)
- [Prerequisites](#prerequisites)
- [Tech Stack](#tech-stack)
- [Getting Started](#getting-started)
- [Environment Variables](#environment-variables)
- [Database Setup](#database-setup)
- [API Documentation](#api-documentation)
- [Project Structure](#project-structure)
- [Testing](#testing)
- [Code Formatting](#code-formatting)

## Architecture

Rafiq Backend follows a **Modular Monolith Architecture** pattern, organizing code into focused, domain-driven modules with clear boundaries:

- **Medication Module**: Medicine inventory, drug search, medication groups, and reminders
- **Lab & Lab Test Modules**: Lab test management and AI-powered result analysis
- **Doctor Module**: Doctor profiles, specializations, and event listeners
- **Patient Module**: Patient profiles and medical history
- **User Module**: User authentication, registration, and verification
- **Notification Module**: Email notifications and alerts
- **File Management Module**: File upload, storage, and retrieval
- **Security Module**: JWT authentication, OAuth2, and security configurations
- **AI Module**: Google Gemini AI integration for medical analysis
- **Shared Module**: Common utilities, DTOs, and cross-cutting concerns

Each module is self-contained with its own:
- API layer (controllers)
- Service layer (business logic)
- Repository layer (data access)
- Entity models
- DTOs and mappers (where applicable)
- Module-specific exceptions

## Prerequisites

- Java 17 or higher
- Maven 3.6+
- PostgreSQL 15

## Tech Stack

- **Framework**: Spring Boot 3.5.6
- **Language**: Java 17
- **Database**: PostgreSQL 15
- **ORM**: Spring Data JPA with Hibernate
- **Migration**: Flyway
- **Security**: Spring Security with JWT & OAuth2
- **AI**: Google Gemini AI (Spring AI)
- **Storage**: Cloudinary
- **PDF Processing**: Apache PDFBox, iTextPDF
- **Email**: Spring Mail with Thymeleaf templates
- **API Documentation**: SpringDoc OpenAPI
- **Testing**: JUnit 5, Testcontainers
- **Job Scheduling**: Spring Quartz
- **Rate Limiting**: Bucket4j
- **HTTP Client**: OpenFeign
- **Mapper**: MapStruct
- **Code Quality**: Spotless, JaCoCo
- **Build Tool**: Maven

## Getting Started

### Step 1: Setup PostgreSQL Database

1. Install PostgreSQL 15
2. Create a database:
   ```sql
   CREATE DATABASE rafiq;
   ```

### Step 2: Configure Environment Variables

Create a `.env.properties` file in the project root with the following variables:

```properties
# Database Configuration
DATASOURCE_URL=jdbc:postgresql://localhost:5432/rafiq
DATASOURCE_USERNAME=postgres
DATASOURCE_PASSWORD=your_password

# JWT Configuration
JWT_SECRET=your_jwt_secret_key_min_32_chars
JWT_EXPIRATION=3600000

# Refresh Token Configuration
REFRESH_EXPIRATION=600000000

# OTP Configuration
OTP_EXPIRATION=300000

# Access Token Configuration
ACCESS_TOKEN_EXPIRATION=360000

# OAuth2 Google Configuration
GOOGLE_CLIENT_ID=your_google_client_id
GOOGLE_CLIENT_SECRET=your_google_client_secret

# OAuth2 Facebook Configuration
FACEBOOK_CLIENT_ID=your_facebook_client_id
FACEBOOK_CLIENT_SECRET=your_facebook_client_secret

# Cloudinary Configuration
CLOUDINARY_CLOUD_NAME=your_cloudinary_cloud_name
CLOUDINARY_API_KEY=your_cloudinary_api_key
CLOUDINARY_API_SECRET=your_cloudinary_api_secret

# Google GenAI Configuration
GOOGLE_GENAI_API_KEY=your_google_genai_api_key

# Mail Configuration (for local SMTP)
MAIL_HOST=localhost
MAIL_PORT=1025
MAIL_USERNAME=your_email
MAIL_PASSWORD=your_password
MAIL_PROTOCOL=smtp
```

### Step 3: Build and Run

1. **Clean and build the project**
   ```bash
   ./mvnw clean install
   ```

2. **Run the application**
   ```bash
   ./mvnw spring-boot:run
   ```

   Or run the JAR directly:
   ```bash
   java -jar target/rafiq-0.0.1-SNAPSHOT.jar
   ```

3. **Access the application**
   - API: http://localhost:8030
   - Swagger UI: http://localhost:8030/swagger-ui.html

## Environment Variables

| Variable | Description | Required | Default |
|----------|-------------|----------|---------|
| `DATASOURCE_URL` | PostgreSQL JDBC URL | Yes | - |
| `DATASOURCE_USERNAME` | Database username | Yes | - |
| `DATASOURCE_PASSWORD` | Database password | Yes | - |
| `JWT_SECRET` | Secret key for JWT signing (min 32 chars) | Yes | - |
| `JWT_EXPIRATION` | JWT token expiration in ms | Yes | 3600000 (1 hour) |
| `REFRESH_EXPIRATION` | Refresh token expiration in ms | Yes | 600000000 (~7 days) |
| `OTP_EXPIRATION` | OTP expiration in ms | Yes | 300000 (5 min) |
| `ACCESS_TOKEN_EXPIRATION` | Access token expiration in ms | Yes | 360000 (6 min) |
| `GOOGLE_CLIENT_ID` | Google OAuth2 Client ID | Yes | - |
| `GOOGLE_CLIENT_SECRET` | Google OAuth2 Client Secret | No | - |
| `FACEBOOK_CLIENT_ID` | Facebook OAuth2 Client ID | Yes | - |
| `FACEBOOK_CLIENT_SECRET` | Facebook OAuth2 Client Secret | Yes | - |
| `CLOUDINARY_CLOUD_NAME` | Cloudinary cloud name | Yes | - |
| `CLOUDINARY_API_KEY` | Cloudinary API key | Yes | - |
| `CLOUDINARY_API_SECRET` | Cloudinary API secret | Yes | - |
| `GOOGLE_GENAI_API_KEY` | Google Gemini AI API key | Yes | - |
| `MAIL_HOST` | SMTP server host | Yes | - |
| `MAIL_PORT` | SMTP server port | Yes | - |
| `MAIL_USERNAME` | SMTP username | Yes | - |
| `MAIL_PASSWORD` | SMTP password | Yes | - |
| `MAIL_PROTOCOL` | Mail protocol | Yes | smtp |

## Database Setup

The application uses **Hibernate DDL auto** for development (set to `create-drop`). For production, enable Flyway migrations:

1. In `application.yml`, set:
   ```yaml
   spring:
     flyway:
       enabled: true
     jpa:
       hibernate:
         ddl-auto: validate
   ```

2. Migration scripts are located in: `src/main/resources/db/migration/`

## API Documentation

### Swagger/OpenAPI

Access interactive API documentation at:
- **Swagger UI**: http://localhost:8030/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8030/v3/api-docs
- **OpenAPI YAML**: http://localhost:8030/v3/api-docs.yaml

### Generate OpenAPI Documentation

To generate OpenAPI documentation files:

```bash
./mvnw spring-boot:run -Dspring-boot.run.arguments=generate-openapi
```

The generated files will be in the `openapi/` directory:
- `openapi.json`
- `openapi.yaml`

### View Documentation

You can view the OpenAPI YAML file at:
https://editor.swagger.io/

### API Endpoints

The application provides the following API endpoints organized by module:

#### Authentication (`/auth/*`)
- Login, Logout, Token Refresh
- OAuth2 (Google/Facebook)
- Password reset and change
- Email verification

#### User Management (`/users/*`)
- Doctor and Patient registration
- Profile management
- OTP verification

#### Medication (`/medicines/*`, `/groups/*`, `/reminders/*`)
- Medicine inventory management
- Drug search and information
- Medication groups and categorization
- Medication reminders and tracking
- Dose scheduling and history

#### Lab Tests (`/lab-tests/*`)
- Upload and manage lab test documents
- AI-powered test result analysis
- Test history and retrieval

#### Doctor (`/doctors/*`, `/specializations/*`)
- Doctor profiles and specializations
- Medical specializations directory

#### Asset Management (`/assets/*`)
- File upload and retrieval
- Image and document management

Sample HTTP requests are available in the `api/` directory.

## Testing

### Run All Tests

```bash
./mvnw test
```

### Run Integration Tests

```bash
./mvnw verify
```

### Run with Coverage

The project uses JaCoCo for code coverage:

```bash
./mvnw clean test jacoco:report
```

View coverage report: `target/site/jacoco/index.html`

## Code Formatting

The project uses **Spotless** with Eclipse formatter for code formatting.

### Check code format

```bash
./mvnw spotless:check
```

### Apply code formatting

```bash
./mvnw spotless:apply
```

### Pre-commit Hook

A pre-commit hook is configured to automatically run Spotless before commits.

## Project Structure

```
rafiq-backend/
├── api/                          # HTTP request samples
│   ├── auth/                     # Authentication endpoints
│   ├── drug/                     # Drug search endpoints
│   ├── group/                    # Medication group endpoints
│   ├── lab-test/                 # Lab test endpoints
│   ├── medicine/                 # Medicine management endpoints
│   ├── reminder/                 # Medication reminder endpoints
│   ├── specialization/           # Specialization endpoints
│   ├── user/                     # User management endpoints
│   └── asset/                    # Asset management endpoints
├── openapi/                      # OpenAPI documentation files
├── src/
│   ├── main/
│   │   ├── java/com/nexaworks/rafiq/
│   │   │   ├── RafiqApplication.java    # Main application entry point
│   │   │   ├── medication/              # Medication module
│   │   │   │   ├── api/                 # Controllers
│   │   │   │   ├── service/             # Business logic
│   │   │   │   ├── repository/          # Data access
│   │   │   │   ├── entity/              # Domain models
│   │   │   │   ├── mapper/              # DTO mappers
│   │   │   │   └── exception/           # Module exceptions
│   │   │   ├── labTest/                 # Lab Test module
│   │   │   │   ├── api/
│   │   │   │   ├── service/
│   │   │   │   ├── repository/
│   │   │   │   ├── entity/
│   │   │   │   ├── mapper/
│   │   │   │   └── exception/
│   │   │   ├── lab/                     # Lab module
│   │   │   │   ├── api/
│   │   │   │   ├── service/
│   │   │   │   ├── repository/
│   │   │   │   ├── entity/
│   │   │   │   └── exception/
│   │   │   ├── doctor/                  # Doctor module
│   │   │   │   ├── api/
│   │   │   │   ├── service/
│   │   │   │   ├── repository/
│   │   │   │   ├── entity/
│   │   │   │   ├── listener/
│   │   │   │   └── exception/
│   │   │   ├── patient/                 # Patient module
│   │   │   │   ├── api/
│   │   │   │   ├── service/
│   │   │   │   ├── repository/
│   │   │   │   ├── entity/
│   │   │   │   └── exception/
│   │   │   ├── user/                    # User management module
│   │   │   │   ├── api/
│   │   │   │   ├── service/
│   │   │   │   ├── repository/
│   │   │   │   ├── entity/
│   │   │   │   └── exception/
│   │   │   ├── notification/            # Notification module
│   │   │   │   ├── service/
│   │   │   │   └── entity/
│   │   │   ├── fileManagment/           # File management module
│   │   │   │   ├── api/
│   │   │   │   ├── service/
│   │   │   │   └── entity/
│   │   │   ├── security/                # Security module
│   │   │   │   ├── config/              # Security configuration
│   │   │   │   ├── jwt/                 # JWT handling
│   │   │   │   ├── oauth2/              # OAuth2 configuration
│   │   │   │   └── filter/              # Security filters
│   │   │   ├── shared/                  # Shared module
│   │   │   │   ├── dto/                 # Common DTOs
│   │   │   │   ├── exception/           # Global exceptions
│   │   │   │   ├── utils/               # Utility classes
│   │   │   │   └── config/              # Shared configurations
│   │   │   ├── ai/                      # AI integration module
│   │   │   │   └── service/             # AI services (Gemini)
│   │   │   ├── config/                  # Application configurations
│   │   │   └── db/                      # Database configurations
│   │   └── resources/
│   │       ├── application.yml          # Application configuration
│   │       ├── db/migration/            # Flyway migrations
│   │       ├── templates/               # Email templates
│   │       └── static/                  # Static resources
│   └── test/
│       ├── java/                        # Test classes
│       └── resources/                   # Test resources
├── target/                              # Build output
├── docker-compose.yml                   # Docker compose configuration
├── Dockerfile                           # Docker image definition
├── pom.xml                              # Maven configuration
├── eclipse-formatter.xml                # Code formatter config
└── README.md                            # This file
```

## Development Tips

1. **Hot Reload**: The project includes Spring Boot DevTools for automatic restart during development.

2. **Modular Architecture**: Each module is independent with clear boundaries. When adding features:
   - Keep module dependencies minimal
   - Use the shared module for cross-cutting concerns
   - Follow the existing package structure within modules

3. **Database Migrations**: Use Flyway for production deployments. Keep migration scripts versioned.

4. **API Testing**: Use the provided `.http` files in the `api/` directory with IntelliJ HTTP Client or VS Code REST Client extension.

5. **Logging**: Check application logs for debugging. Adjust log levels in `application.yml`.

6. **Rate Limiting**: The application uses Bucket4j for API rate limiting. Configure in the application properties.

7. **Docker Support**: Use the provided `docker-compose.yml` for containerized development and deployment.

## Troubleshooting

### Port Already in Use
If port 8030 is already in use, change it in `application.yml`:
```yaml
server:
  port: 8031
```

### Database Connection Issues
- Ensure PostgreSQL is running
- Verify database credentials
- Check if port 5432 is accessible

### Maven Build Issues
Clear Maven cache and rebuild:
```bash
./mvnw clean install -U
```

