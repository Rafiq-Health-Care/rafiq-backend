package com.nexaworks.rafiq.shared.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.nexaworks.rafiq.shared.exception.model.ErrorResponse;
import com.nexaworks.rafiq.shared.exception.model.ValidationErrorResponse;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;

/**
 * OpenAPI configuration for Rafiq Healthcare Application
 *
 * Provides comprehensive API documentation with organized tags, global error
 * responses, and security schemes.
 */
@Configuration
@OpenAPIDefinition(info = @Info(title = "Rafiq Healthcare API", version = "1.0.0", description = """
        # Rafiq Healthcare Application API

        A comprehensive healthcare management system designed to help patients and doctors manage medical information efficiently.

        ## Key Features

        ### 🔐 Authentication & Authorization
        - Secure JWT-based authentication with HTTP-only cookies
        - OAuth2 integration (Google, Facebook)
        - Role-based access control (Patient, Doctor)
        - Token refresh mechanism for seamless sessions

        ### 👤 User Management
        - Patient registration with email verification (OTP)
        - Doctor registration with specialization and national ID upload
        - Profile management and medical history tracking

        ### 🧪 Lab Test Management
        - AI-powered extraction of lab results from PDF/images using Google Gemini AI
        - OCR processing with Tesseract for document text extraction
        - Structured storage and retrieval of test results
        - Support for multiple test types and date tracking

        ### 💊 Medication Management
        - Track medicines with dosage, frequency, and schedules
        - Create medicine groups for better organization
        - Set up automated medication reminders
        - Support for custom schedules (specific days, times)
        - Medicine types: Tablet, Capsule, Liquid, Injection, Topical, Inhaler, etc.

        ### 📁 File Management
        - Upload medical documents (PDF, images)
        - AI-powered document analysis and data extraction
        - Cloudinary integration for secure file storage
        - Automatic lab test result parsing from documents

        ### 📊 Patient Profiles
        - Complete medical profile management
        - Weight history tracking
        - Blood type, health conditions, and lifestyle information
        - Emergency contact management

        ## Authentication

        The API uses JWT-based authentication with HTTP-only cookies for secure token storage.
        Token format: JWT tokens with user ID (UUID) as principal.

        ## Data Formats

        - **Dates**: ISO 8601 format (e.g., `2025-01-15T10:30:00Z`)
        - **UUIDs**: Standard UUID v4 format
        - **Enums**: See documentation for allowed values
        - **Pagination**: Page-based with configurable size and sorting

        ## Rate Limiting

        API requests are rate-limited using Bucket4j. Check response headers:
        - `X-RateLimit-Remaining`: Remaining requests
        - `X-RateLimit-Reset`: Reset time in seconds

        ## Error Handling

        The API uses a consistent error response format for all error scenarios. All error responses include:
        - **status**: HTTP status code
        - **error**: HTTP status reason phrase
        - **message**: Human-readable error message
        - **timestamp**: ISO 8601 timestamp when the error occurred
        - **path**: The request path that caused the error
        - **validationErrors**: (for 400 errors only) Map of field names to validation error messages

        ### Error Response Structure

        **Standard Error Response:**
        ```json
        {
          "status": 404,
          "error": "Not Found",
          "message": "Resource not found",
          "timestamp": "2025-01-15T14:30:45.123Z",
          "path": "/api/resource/123"
        }
        ```

        **Validation Error Response (400):**
        ```json
        {
          "status": 400,
          "error": "Bad Request",
          "message": "Validation failed",
          "timestamp": "2025-01-15T14:30:45.123Z",
          "validationErrors": {
            "email": "must be a valid email address",
            "password": "must be at least 8 characters long"
          }
        }
        ```

        ### HTTP Status Codes

        | Code | Status | Description | When It Occurs |
        |------|--------|-------------|----------------|
        | **400** | Bad Request | Invalid input or validation failure | Missing required fields, invalid format, validation constraints violated, empty file uploads, unsupported file types |
        | **401** | Unauthorized | Authentication required or failed | Missing/invalid JWT token, expired token, invalid credentials, invalid OAuth token |
        | **403** | Forbidden | Insufficient permissions | User lacks required role (e.g., DOCTOR vs PATIENT), access denied to resource |
        | **404** | Not Found | Resource does not exist | User, medicine, reminder, group, or other resource not found or not accessible |
        | **405** | Method Not Allowed | HTTP method not supported | Using GET on POST-only endpoint, PUT on DELETE-only endpoint, etc. |
        | **409** | Conflict | Resource state conflict | Email already registered, medicine already in list, group name already exists, duplicate resource |
        | **422** | Unprocessable Entity | Business logic violation | Medicine limit exceeded, invalid business state, cannot process request |
        | **429** | Too Many Requests | Rate limit exceeded | Too many requests in time window, check X-RateLimit-Reset header |
        | **500** | Internal Server Error | Server-side error | Unexpected server error, database issues, external service failures |

        ### Common Error Scenarios

        **Validation Errors (400):**
        - Invalid email format
        - Password doesn't meet requirements (min 8 chars, uppercase, lowercase, number, special char)
        - Invalid phone number format (must be international: +1234567890)
        - Invalid gender value (must be 'male' or 'female')
        - Age validation (must be 18+)
        - Missing required fields
        - Invalid date format
        - Empty file uploads
        - Unsupported file types

        **Authentication Errors (401):**
        - Missing Authorization header
        - Invalid JWT token format
        - Expired access token (use refresh token)
        - Invalid refresh token
        - Invalid login credentials
        - Invalid OAuth token (Google/Facebook)

        **Authorization Errors (403):**
        - Patient trying to access doctor-only resources
        - Doctor trying to access patient-specific resources
        - User trying to access another user's private data

        **Not Found Errors (404):**
        - User not found
        - Medicine not found
        - Reminder not found
        - Group not found
        - Lab test not found
        - Invalid resource ID format

        **Conflict Errors (409):**
        - Email already registered
        - Medicine already exists in user's list
        - Group name already exists
        - Duplicate resource creation

        **Unprocessable Entity (422):**
        - Medicine limit exceeded (user has reached maximum allowed medicines)
        - Invalid business state transition
        - Cannot process due to business rules

        **Rate Limiting (429):**
        - Too many login attempts
        - Too many API requests in short time
        - Check `X-RateLimit-Remaining` header for remaining requests
        - Check `X-RateLimit-Reset` header for retry time (seconds)

        ### Error Handling Best Practices

        1. **Always check the status code** first to determine error type
        2. **For 400 errors**, check the `validationErrors` object for field-specific issues
        3. **For 401 errors**, attempt to refresh the token or re-authenticate
        4. **For 403 errors**, verify user has correct role/permissions
        5. **For 404 errors**, verify resource ID exists and user has access
        6. **For 409 errors**, handle duplicate resource scenarios gracefully
        7. **For 429 errors**, implement exponential backoff retry logic
        8. **For 500 errors**, log error details and retry with backoff, contact support if persistent
        9. **Always log the timestamp and path** for debugging purposes
        10. **Display user-friendly messages** based on the error type

        ## Support

        For issues or questions, contact: elbialy@gmail.com
        """, contact = @Contact(name = "Rafiq Support", email = "elbialy@gmail.com", url = "https://github.com/MahmoudElbialy"), license = @License(name = "MIT License", url = "https://github.com/MahmoudElbialy/rafiq-backend/blob/main/LICENSE"), termsOfService = "https://github.com/MahmoudElbialy/rafiq-backend"), servers = {
        @Server(description = "Local Development Server", url = "http://localhost:8030", variables = {}),
        @Server(description = "Production Server", url = "https://rafiq.herokuapp.com", variables = {})}, security = {
                @SecurityRequirement(name = "bearerAuth")})
@SecurityScheme(name = "bearerAuth", description = """
        JWT Bearer Token Authentication

        The API uses JWT tokens for authentication. Access tokens are short-lived and refresh tokens are long-lived.
        Tokens are stored in HTTP-only cookies for security. Include the access token in the Authorization header:
        `Authorization: Bearer <access-token>`

        Token format: JWT with user UUID as principal.
        """, scheme = "bearer", type = SecuritySchemeType.HTTP, bearerFormat = "JWT", in = SecuritySchemeIn.HEADER)
public class OpenApiConfig {

    /**
     * Configures global OpenAPI components including error response schemas
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI().components(new Components()
                .addResponses("BadRequest",
                        createErrorResponse("400", "Bad Request",
                                """
                                        Validation failed or invalid input. Request contains invalid data or violates validation constraints.

                                        **Response includes:**
                                        - `validationErrors`: Map of field names to specific validation error messages

                                        **Common validation errors:**
                                        - Email format validation
                                        - Password strength requirements (min 8 chars, uppercase, lowercase, number, special char)
                                        - Phone number format (international format: +1234567890)
                                        - Gender validation (must be 'male' or 'female')
                                        - Age validation (must be 18+)
                                        - Required field missing
                                        - Invalid date format
                                        - Empty file uploads
                                        - Unsupported file types
                                        - Invalid UUID format
                                        - Out of range values

                                        **Resolution:**
                                        - Check `validationErrors` object for field-specific issues
                                        - Fix validation errors and retry request
                                        - Ensure all required fields are provided
                                        - Verify data formats match requirements
                                        """,
                                ValidationErrorResponse.class,
                                """
                                        {
                                          "status": 400,
                                          "error": "Bad Request",
                                          "message": "Validation failed",
                                          "timestamp": "2025-01-15T14:30:45.123Z",
                                          "validationErrors": {
                                            "email": "must be a valid email address",
                                            "password": "must be at least 8 characters long and include uppercase, lowercase, number, and special character",
                                            "phone": "Invalid phone number format. Use international format: +1234567890",
                                            "gender": "Gender must be either 'male' or 'female'",
                                            "birthDate": "Age must be at least 18 years"
                                          }
                                        }
                                        """))
                .addResponses("Unauthorized", createErrorResponse("401", "Unauthorized", """
                        Authentication required or token invalid/expired.

                        **Common causes:**
                        - Missing Authorization header
                        - Invalid JWT token format
                        - Expired access token (use refresh token endpoint)
                        - Invalid refresh token
                        - Invalid login credentials
                        - Invalid OAuth token

                        **Resolution:**
                        - Re-authenticate using login endpoint
                        - Use refresh token endpoint to get new access token
                        - Verify token is included in Authorization header: `Bearer <token>`
                        """, ErrorResponse.class, """
                        {
                          "status": 401,
                          "error": "Unauthorized",
                          "message": "Invalid or expired JWT token",
                          "timestamp": "2025-01-15T14:30:45.123Z",
                          "path": "/api/resource"
                        }
                        """))
                .addResponses("Forbidden",
                        createErrorResponse("403", "Forbidden",
                                """
                                        Insufficient permissions to access this resource.

                                        **Common causes:**
                                        - User role doesn't match required role (e.g., PATIENT trying to access DOCTOR-only resource)
                                        - User trying to access another user's private data
                                        - Resource ownership mismatch

                                        **Resolution:**
                                        - Verify user has correct role (PATIENT or DOCTOR)
                                        - Ensure user owns the resource they're trying to access
                                        - Check if resource requires specific permissions
                                        """,
                                ErrorResponse.class,
                                """
                                        {
                                          "status": 403,
                                          "error": "Forbidden",
                                          "message": "You don't have permission to access this resource. Required role: DOCTOR",
                                          "timestamp": "2025-01-15T14:30:45.123Z",
                                          "path": "/api/resource"
                                        }
                                        """))
                .addResponses("NotFound", createErrorResponse("404", "Not Found", """
                        The requested resource does not exist or you don't have access to it.

                        **Common causes:**
                        - Resource ID doesn't exist in database
                        - Resource was deleted
                        - User doesn't have access to the resource (belongs to another user)
                        - Invalid UUID format
                        - Resource type mismatch

                        **Resolution:**
                        - Verify resource ID is correct and exists
                        - Check if resource belongs to current user
                        - Ensure UUID format is valid (e.g., 550e8400-e29b-41d4-a716-446655440000)
                        - Verify resource hasn't been deleted
                        """, ErrorResponse.class,
                        """
                                {
                                  "status": 404,
                                  "error": "Not Found",
                                  "message": "Medicine not found with id: 550e8400-e29b-41d4-a716-446655440000",
                                  "timestamp": "2025-01-15T14:30:45.123Z",
                                  "path": "/api/resource/550e8400-e29b-41d4-a716-446655440000"
                                }
                                """))
                .addResponses("Conflict", createErrorResponse("409", "Conflict", """
                        Resource already exists or state conflict.

                        **Common causes:**
                        - Email already registered
                        - Medicine already exists in user's medication list
                        - Group name already exists for the user
                        - Duplicate resource creation
                        - Resource state prevents operation

                        **Resolution:**
                        - Use existing resource instead of creating duplicate
                        - Choose different email/name if creating new resource
                        - Update existing resource instead of creating new one
                        - Check resource state before operation
                        """, ErrorResponse.class, """
                        {
                          "status": 409,
                          "error": "Conflict",
                          "message": "Email already registered: patient@example.com",
                          "timestamp": "2025-01-15T14:30:45.123Z",
                          "path": "/api/resource"
                        }
                        """))
                .addResponses("MethodNotAllowed",
                        createErrorResponse("405", "Method Not Allowed", """
                                HTTP method not allowed for this resource.

                                **Common causes:**
                                - Using GET on POST-only endpoint
                                - Using PUT on DELETE-only endpoint
                                - Using unsupported HTTP method

                                **Resolution:**
                                - Check API documentation for correct HTTP method
                                - Verify endpoint supports the method you're using
                                - Use appropriate method (GET, POST, PUT, DELETE, PATCH)
                                """, ErrorResponse.class, """
                                {
                                  "status": 405,
                                  "error": "Method Not Allowed",
                                  "message": "Request method 'PUT' not supported",
                                  "timestamp": "2025-01-15T14:30:45.123Z",
                                  "path": "/api/resource"
                                }
                                """))
                .addResponses("UnprocessableEntity",
                        createErrorResponse("422", "Unprocessable Entity",
                                """
                                        Request is well-formed but cannot be processed due to business logic violations.

                                        **Common causes:**
                                        - Medicine limit exceeded (user reached maximum allowed medicines)
                                        - Invalid business state transition
                                        - Business rule violation
                                        - Cannot process due to constraints

                                        **Resolution:**
                                        - Remove existing medicines before adding new ones if limit reached
                                        - Verify business state allows the operation
                                        - Check business rules and constraints
                                        - Contact support if limit needs to be increased
                                        """,
                                ErrorResponse.class, """
                                        {
                                          "status": 422,
                                          "error": "Unprocessable Entity",
                                          "message": "Medicine limit exceeded. Maximum allowed: 50",
                                          "timestamp": "2025-01-15T14:30:45.123Z",
                                          "path": "/api/resource"
                                        }
                                        """))
                .addResponses("TooManyRequests", createErrorResponse("429", "Too Many Requests", """
                        Rate limit exceeded. Too many requests in the configured time window.

                        **Response Headers:**
                        - `X-RateLimit-Remaining`: Number of requests remaining in current window
                        - `X-RateLimit-Reset`: Time in seconds until rate limit resets

                        **Resolution:**
                        - Wait for the rate limit window to reset
                        - Implement exponential backoff retry logic
                        - Reduce request frequency
                        - Check X-RateLimit-Reset header for exact retry time
                        """, ErrorResponse.class, """
                        {
                          "status": 429,
                          "error": "Too Many Requests",
                          "message": "Rate limit exceeded. Please try again later.",
                          "timestamp": "2025-01-15T14:30:45.123Z",
                          "path": "/api/resource"
                        }
                        """))
                .addResponses("InternalServerError", createErrorResponse("500",
                        "Internal Server Error",
                        """
                                An unexpected server error occurred. This indicates a problem on the server side.

                                **Common causes:**
                                - Database connection issues
                                - External service failures (AI service, file storage, email service)
                                - Server configuration problems
                                - Unexpected exceptions in application code
                                - Resource exhaustion

                                **Resolution:**
                                - Retry the request after a short delay
                                - Implement exponential backoff retry logic
                                - Check server status and health
                                - If error persists, contact support with:
                                  * Error timestamp
                                  * Request path
                                  * Request details (if safe to share)
                                  * Error message
                                - Do not retry immediately for 500 errors
                                """,
                        ErrorResponse.class,
                        """
                                {
                                  "status": 500,
                                  "error": "Internal Server Error",
                                  "message": "An unexpected error occurred. Please try again later.",
                                  "timestamp": "2025-01-15T14:30:45.123Z",
                                  "path": "/api/resource"
                                }
                                """)));
    }

    /**
     * Creates a standardized error response component
     */
    private ApiResponse createErrorResponse(String statusCode, String error, String description,
            Class<?> schemaClass, String exampleJson) {
        Schema<?> schema = new Schema<>();
        schema.set$ref("#/components/schemas/" + schemaClass.getSimpleName());

        MediaType mediaType = new MediaType().schema(schema);

        // Set example if provided
        if (exampleJson != null && !exampleJson.trim().isEmpty()) {
            try {
                // Parse JSON string to Object for proper example formatting
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                Object exampleObj = mapper.readValue(exampleJson, Object.class);
                mediaType.setExample(exampleObj);
            } catch (Exception e) {
                // If parsing fails, use string as-is
                mediaType.setExample(exampleJson);
            }
        }

        Content content = new Content().addMediaType("application/json", mediaType);

        return new ApiResponse().description(description).content(content);
    }
}
