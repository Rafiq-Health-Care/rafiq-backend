# Rafiq Backend - Healthcare Application

A Spring Boot-based healthcare application that provides medical services including lab test management, doctor registration, patient management, and AI-powered medical document analysis.

## Table of Contents
- [Prerequisites](#prerequisites)
- [Tech Stack](#tech-stack)
- [Getting Started](#getting-started)
- [Environment Variables](#environment-variables)
- [Database Setup](#database-setup)
- [API Documentation](#api-documentation)
- [Testing](#testing)
- [Code Formatting](#code-formatting)

## Prerequisites

- Java 17 or higher
- Maven 3.6+
- PostgreSQL 15
- Tesseract OCR 5.x (for document processing)

## Tech Stack

- **Framework**: Spring Boot 3.5.6
- **Language**: Java 17
- **Database**: PostgreSQL 15
- **ORM**: Spring Data JPA with Hibernate
- **Migration**: Flyway
- **Security**: Spring Security with JWT & OAuth2
- **AI**: Google Gemini AI (GenAI)
- **Storage**: Cloudinary
- **OCR**: Tesseract & Tess4j
- **PDF Processing**: Apache PDFBox, iTextPDF
- **Email**: Spring Mail with Thymeleaf templates
- **API Documentation**: SpringDoc OpenAPI
- **Testing**: JUnit 5
- **Rate Limiting**: Bucket4j
- **HTTP Client**: OpenFeign
- **Build Tool**: Maven

## Getting Started

### Step 1: Install Tesseract OCR

**On Ubuntu/Debian:**
```bash
sudo apt-get update
sudo apt-get install -y tesseract-ocr tesseract-ocr-eng
```

**On macOS:**
```bash
brew install tesseract
```

**On Windows:**
Download and install from: https://github.com/UB-Mannheim/tesseract/wiki

### Step 2: Setup PostgreSQL Database

1. Install PostgreSQL 15
2. Create a database:
   ```sql
   CREATE DATABASE rafiq;
   ```

### Step 3: Configure Environment Variables

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

### Step 4: Build and Run

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
mvn spring-boot:run -Dspring-boot.run.arguments=generate-openapi
```

The generated files will be in the `openapi/` directory:
- `openapi.json`
- `openapi.yaml`

### View Documentation

You can view the OpenAPI YAML file at:
https://editor.swagger.io/

### API Endpoints

The application provides the following API endpoints:

- **Authentication**: `/auth/*`
  - Login, Register, OAuth2 (Google/Facebook)
  - Password reset, Email verification
  
- **User Management**: `/users/*`
  - Doctor and Patient registration
  - Profile management
  
- **Lab Tests**: `/lab-tests/*`
  - Upload, retrieve, update, delete lab tests
  - AI-powered test result analysis
  
- **Specializations**: `/specializations/*`
  - Medical specializations management

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
│   ├── lab-test/                 # Lab test endpoints
│   ├── specialization/           # Specialization endpoints
│   └── user/                     # User management endpoints
├── openapi/                      # OpenAPI documentation files
├── src/
│   ├── main/
│   │   ├── java/com/nexaworks/rafiq/
│   │   │   ├── config/           # Configuration classes
│   │   │   ├── controller/       # REST controllers
│   │   │   ├── dto/              # Data Transfer Objects
│   │   │   ├── entities/         # JPA entities
│   │   │   ├── enums/            # Enumerations
│   │   │   ├── eventListener/    # Event listeners
│   │   │   ├── exception/        # Custom exceptions
│   │   │   ├── mapper/           # MapStruct mappers
│   │   │   ├── security/         # Security configuration
│   │   │   ├── service/          # Business logic
│   │   │   └── utils/            # Utility classes
│   │   └── resources/
│   │       ├── application.yml   # Application configuration
│   │       ├── db/migration/     # Flyway migrations
│   │       └── templates/        # Email templates
│   └── test/                     # Test classes
├── pom.xml                       # Maven configuration
└── README.md                     # This file
```

## Development Tips

1. **Hot Reload**: The project includes Spring Boot DevTools for automatic restart during development.

2. **Database Migrations**: Use Flyway for production deployments. Keep migration scripts versioned.

3. **API Testing**: Use the provided `.http` files in the `api/` directory with IntelliJ HTTP Client or VS Code REST Client.

4. **Logging**: Check application logs for debugging. Adjust log levels in `application.yml`.

5. **Rate Limiting**: The application uses Bucket4j for API rate limiting. Configure in the application properties.

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

### Tesseract Not Found
Set the `TESSDATA_PREFIX` environment variable:
```bash
export TESSDATA_PREFIX=/usr/share/tesseract-ocr/5/tessdata
```

### Maven Build Issues
Clear Maven cache and rebuild:
```bash
./mvnw clean install -U
```

## Contributing

1. Follow the existing code style (enforced by Spotless)
2. Write tests for new features
3. Update API documentation
4. Ensure all tests pass before submitting PR

## License

[Add your license information here]

## Support

For issues and questions, please contact the development team or create an issue in the repository.
