# Rafiq — Healthcare Platform Backend

> Graduation Project Documentation  
> Spring Boot · Java 17 · PostgreSQL · RabbitMQ · Redis

---

## Table of Contents

1. [Project Overview](#1-project-overview)
2. [Tech Stack](#2-tech-stack)
3. [System Architecture](#3-system-architecture)
4. [Module Breakdown](#4-module-breakdown)
   - 4.1 [Authentication & Security](#41-authentication--security)
   - 4.2 [Consultation & Booking](#42-consultation--booking)
   - 4.3 [Payment & Refund](#43-payment--refund)
   - 4.4 [Lab Tests & AI Analysis](#44-lab-tests--ai-analysis)
   - 4.5 [Medicine & Reminders](#45-medicine--reminders)
   - 4.6 [Notifications](#46-notifications)
   - 4.7 [Video Calls](#47-video-calls)
5. [Database Design](#5-database-design)
6. [Key Design Decisions](#6-key-design-decisions)
7. [Security Model](#7-security-model)
8. [API Reference](#8-api-reference)
9. [Running the Project](#9-running-the-project)
10. [Project Structure](#10-project-structure)

---

## 1. Project Overview

**Rafiq** (Arabic: رفيق, meaning "companion") is a full-featured healthcare platform backend that connects patients with doctors through a digital interface. It was built as a graduation project to demonstrate production-grade software engineering practices in the healthcare domain.

### What it does

- Patients can search doctors by specialization, view their profiles and availability, and book online consultations.
- Doctors manage their schedule by creating consultation time slots, and receive payouts after completed sessions.
- Consultations happen via live video calls powered by Agora RTC.
- Patients upload medical lab reports (PDF), and an AI model (Google Gemini) automatically extracts and structures the test results.
- A medicine and reminder system lets patients track their medication schedules.
- All critical events (booking confirmation, cancellation, reminders) trigger multi-channel notifications: email, push (Firebase), and SMS.
- Payments are processed through Stripe with webhook-driven confirmation, and automatic refunds on cancellation.

### Who uses it

| Role | Capabilities |
|---|---|
| Patient | Register, book consultations, upload lab results, track medicines, receive reminders |
| Doctor | Register, manage slots, accept bookings, conduct video consultations, receive payouts |
| System | Schedule jobs, process payments, send notifications, expire stale slots |

---

## 2. Tech Stack

| Category | Technology | Purpose |
|---|---|---|
| Framework | Spring Boot 3.5 | Core application framework |
| Language | Java 17 | Primary language |
| Database | PostgreSQL 15 | Primary data store |
| ORM | Spring Data JPA + Hibernate | Database access |
| Migrations | Flyway | Versioned schema management (43 migrations) |
| Security | Spring Security + JWT | Authentication and authorization |
| OAuth2 | Google, Facebook | Social login |
| Messaging | RabbitMQ | Async notification delivery |
| Cache / Idempotency | Redis | Request deduplication |
| AI | Google Gemini (GenAI) | Lab report PDF extraction |
| File Storage | Cloudinary | Image and document uploads |
| OCR | Tesseract / Tess4j | PDF text extraction fallback |
| PDF Processing | Apache PDFBox, iTextPDF | Document handling |
| Payments | Stripe | Online payment and refunds |
| Video Calls | Agora RTC | Real-time video consultation |
| Push Notifications | Firebase Cloud Messaging | Mobile push notifications |
| Email | Spring Mail + Thymeleaf | Templated emails |
| Scheduling | JobRunr | Background job scheduling |
| Error Tracking | Sentry | Production error monitoring |
| WebSocket | Spring WebSocket + STOMP | Real-time events to frontend |
| Rate Limiting | Bucket4j | API rate limiting |
| API Docs | SpringDoc OpenAPI | Swagger UI |
| Testing | JUnit 5 + Testcontainers | Unit and integration tests |
| Build | Maven + Spotless | Build and code formatting |

---

## 3. System Architecture

The application follows a classic **layered architecture** with clear separation between the HTTP layer, business logic, and data access. It is event-driven for all side effects (notifications, refunds) using RabbitMQ as the message broker.

```
┌─────────────────────────────────────────────────────────┐
│                        Clients                          │
│          Mobile App · Web Browser · WebSocket           │
└───────────────────────┬─────────────────────────────────┘
                        │ HTTP / WS
┌───────────────────────▼─────────────────────────────────┐
│                   Security Layer                        │
│   JWT Filter → Rate Limiter → Idempotency Filter        │
└───────────────────────┬─────────────────────────────────┘
                        │
┌───────────────────────▼─────────────────────────────────┐
│                REST Controllers (17)                    │
│  Auth · User · Doctor · Patient · Consultation ·        │
│  Lab · Medicine · Reminder · Stripe · Notification ...  │
└───────────────────────┬─────────────────────────────────┘
                        │
┌───────────────────────▼─────────────────────────────────┐
│                  Service Layer                          │
│  Business logic · Validation · MapStruct mapping        │
│  Spring Retry · Transaction management                  │
└──────┬──────────┬──────────┬──────────┬─────────────────┘
       │          │          │          │
  ┌────▼───┐ ┌───▼────┐ ┌───▼────┐ ┌───▼──────────────┐
  │ JPA /  │ │Rabbit  │ │ Redis  │ │  External APIs   │
  │Postgres│ │  MQ    │ │ Cache  │ │Stripe·Gemini·    │
  └────────┘ └────────┘ └────────┘ │Agora·Cloudinary  │
                                    └──────────────────┘
```

### Request lifecycle

1. A request arrives and passes through the **JWT filter** (validates the cookie-based token).
2. The **rate limiting filter** (Bucket4j) enforces per-IP/user request quotas.
3. For mutating endpoints, the **idempotency filter** checks whether an identical request was already processed and returns the cached response if so.
4. Spring Security enforces **method-level authorization** (`@PreAuthorize`) for role-based access.
5. The **controller** receives the validated request and delegates to the **service layer**.
6. Services use **JPA repositories** for data access and publish events to **RabbitMQ** for side effects.
7. **Consumers** on RabbitMQ queues handle emails, push notifications, and refunds asynchronously.
8. **JobRunr** runs scheduled jobs for slot expiration, payment expiration, and reminder delivery.

---

## 4. Module Breakdown

### 4.1 Authentication & Security

Authentication uses **stateless JWT tokens delivered via HttpOnly cookies**. This protects against XSS attacks since JavaScript cannot read the token.

#### Login flow

```
POST /api/v1/auth/login
  → AuthServiceImpl.login()
  → AuthenticationManager validates credentials (BCrypt password check)
  → JwtServiceImpl.generateToken()  → JWT cookie set
  → TokenServiceImpl.createRefreshToken() → Refresh token cookie set
  → LoginResponse returned
```

#### Token refresh flow

```
POST /api/v1/auth/refresh
  → Reads "refreshToken" cookie
  → Validates token not expired in DB
  → Issues new JWT + refreshes the refresh token
```

#### Logout

```
POST /api/v1/auth/logout
  → Refresh token invalidated in DB
  → JWT added to JWT_BLACKLIST token table
  → Both cookies cleared
```

**Note:** A blacklist entry is created on logout, but the current `JwtFilter` does not query the blacklist on each request. This is a known limitation — the blacklisted token stays technically valid until its natural expiration.

#### OAuth2 (Google)

```
POST /api/v1/oauth2/google   { idToken }
  → GoogleIdTokenVerifier verifies the token with Google's public keys
  → User is created if first login, or loaded if returning
  → Same session creation as normal login
```

#### Password reset

```
POST /api/v1/password/forget-password  { email }
  → OTP generated and emailed to the user
  → OTP stored as a TOKEN_OTP entry in the token table with expiry

POST /api/v1/password/change-password  { email, otp, newPassword }
  → OTP verified against the token table
  → Password updated with BCrypt
```

#### Security configuration highlights

- Sessions are **stateless** (`SessionCreationPolicy.STATELESS`)
- CSRF is **disabled** (JWT-based auth)
- Password hashing: **BCrypt with cost factor 10**
- Role-based access via Spring Security method security (`@EnableMethodSecurity`)
- Custom `403` and `401` handlers return structured JSON responses

---

### 4.2 Consultation & Booking

This is the core domain of the application. The consultation lifecycle goes through these states:

```
Slot:          AVAILABLE → PENDING_PAYMENT → BOOKED → EXPIRED / CANCELLED
Consultation:  PENDING   → UPCOMING        → LIVE   → COMPLETED / CANCELLED
```

#### Creating a slot (Doctor)

```
POST /api/v1/consultation/slot
  → ConsultationSlotService.add()
  → Pessimistic lock on doctor row (prevents double-creation under race conditions)
  → Overlap check against existing slots
  → Slot saved with status AVAILABLE
  → JobRunr schedules slot expiration at slot.endTime
```

#### Booking a slot (Patient)

```
POST /api/v1/consultation/reserve  { slotId, paymentProvider }
  → ReservationService.reserve()
  → Pessimistic lock on slot row (@Retryable × 3 on PessimisticLockingFailureException)
  → Slot status checked: must be AVAILABLE
  → Patient overlap check (can't book two consultations at the same time)
  → PaymentServiceImpl.process() → Stripe PaymentIntent created
  → Slot → PENDING_PAYMENT, Consultation → PENDING saved
  → Payment expiration job scheduled (if payment not confirmed in time, slot reopens)
  → WebSocket event broadcast to /topic/consultation
  → clientSecret returned to frontend for Stripe SDK
```

#### Payment confirmation (Stripe webhook)

```
POST /stripe/webhook
  → Stripe signature verified
  → payment_intent.succeeded  → ConsultationProcessingService.success()
      → Consultation → UPCOMING, Slot → BOOKED
      → Reminder notification scheduled (JobRunr)
      → Doctor notified via RabbitMQ
  → payment_intent.failed     → ConsultationProcessingService.failed()
      → Consultation → CANCELLED, Slot → AVAILABLE
      → Patient notified via RabbitMQ
```

#### Pre-call preparation (JobRunr, runs ~15 min before)

```
ConsultationPreparationService.prepare(consultationId)
  → Agora token generated (expires at consultation end time)
  → Consultation → LIVE, accessToken stored
```

#### Cancellation

```
DELETE /api/v1/consultation/{id}  { reason }
  → ConsultationCancellationService.cancel()
  → Authorization check: only doctor or patient of that consultation
  → CancellationLog created
  → Consultation → CANCELLED, Slot → AVAILABLE
  → RefundService.refund() → creates RefundRequest entity
  → After commit: RefundEventManager publishes to RabbitMQ refund queue
  → RefundConsumer processes the actual Stripe refund asynchronously
  → Doctor/patient notified of who cancelled
```

---

### 4.3 Payment & Refund

Payments are provider-agnostic: a `PaymentProviderService` interface allows any payment gateway to be plugged in. Currently only Stripe is implemented.

#### Retry policy

Stripe calls use `@Retryable` with exponential backoff:
- Retries on: `RateLimitException`, `ApiConnectionException`
- Max attempts: 3
- Backoff: 1s initial, multiplier 3× (so 1s → 3s → 9s)
- `@Recover` method throws `PaymentProviderException` after all retries exhausted

#### Refund flow

Refunds are processed asynchronously via RabbitMQ to decouple them from the cancellation HTTP request:

```
Cancellation → RefundService.refund() [creates RefundRequest in DB]
            → RefundEventManager publishes RefundRequestEvent to queue
            → RefundConsumer.consume() → StripeService.refund()
            → RefundRequest updated with refund ID and status REFUNDED
```

If the Stripe refund call fails, the message goes to the **Dead Letter Queue (DLQ)**, where it can be reprocessed or inspected.

#### Payout to doctors

After a consultation is completed, doctors receive their earnings via Stripe Connect payouts. A `PayoutScheduler` runs periodically to process pending payouts.

---

### 4.4 Lab Tests & AI Analysis

Patients can upload medical lab reports (PDF or image). The system then uses Google Gemini to automatically extract test results.

#### Upload and analysis flow

```
POST /api/v1/lab-tests  { file (PDF/image) }
  → CloudinaryService uploads the file and returns the URL
  → PdfExtractorServiceImpl extracts raw bytes
  → GeminiService.extractLabResultsFromPdf(bytes)
      → PDF base64-encoded and sent to Gemini API
      → Prompt instructs Gemini to return ONLY a JSON array of tests
      → JSON parsed into List<TestResult> objects
  → LabTest and LabResult entities persisted
  → Response includes the structured test results
```

#### AI extraction prompt (summarized)

The prompt instructs Gemini to:
- Extract all test names, numerical results, units, and status
- Infer status (High/Low/Normal) from standard adult reference ranges when not explicitly stated
- Return only valid JSON — no explanations or markdown
- Handle both numeric results (e.g., `13.5 g/dL`) and non-numeric results (e.g., `Non Reactive`)

#### Example Gemini output

```json
{
  "tests": [
    { "testName": "Hemoglobin", "result": "13.5", "unit": "g/dL", "status": "Normal" },
    { "testName": "Ferritin",   "result": "20",   "unit": "µg/L",  "status": "Low"    },
    { "testName": "HIV",        "result": "Non Reactive", "unit": "", "status": "Non Reactive" }
  ]
}
```

---

### 4.5 Medicine & Reminders

Patients can track their prescribed medications and set up reminders.

#### Data model

```
Medicine
  ├── name, dosage, type (TABLET / SYRUP / INJECTION / ...)
  ├── frequency (DAILY / TWICE_DAILY / WEEKLY / ...)
  ├── status (ACTIVE / INACTIVE / COMPLETED)
  └── Group (optional, for grouping medications by treatment)

Reminder
  ├── medicine (FK)
  ├── scheduledTime
  ├── frequency
  ├── status (PENDING / SENT / ACKNOWLEDGED / MISSED)
  └── ReminderLog (history of each reminder event)
```

#### Reminder delivery

JobRunr schedules jobs for each reminder time. When a reminder fires, a push notification is sent to the patient's device via Firebase Cloud Messaging (FCM). The ReminderLog records whether the notification was acknowledged.

#### Drug database

A drug catalog (`Drug` entity) is seeded from a CSV via a Flyway Java migration (`V34__Import_drugs_from_csv.java`). Patients can search available drugs when adding medicines.

---

### 4.6 Notifications

All notifications are sent asynchronously via RabbitMQ. This ensures that a failed email send never blocks an API response.

#### Architecture

```
Service Layer
  → NotificationManager.publish*(...)
  → RabbitMQ Exchange
       ├── Email Queue     → EmailNotificationConsumer → Spring Mail
       ├── Push Queue      → PushNotificationConsumer  → Firebase FCM
       └── SMS Queue       → SMSNotificationConsumer   → (SMS provider)

Dead Letter Queues (DLQ):
  ├── EmailDLQProcessor  — retries or discards failed emails
  └── PushDLQProcessor   — retries or discards failed push notifications
```

#### Notification events

| Event | Channels |
|---|---|
| Registration | Email (OTP verification) |
| Booking confirmed | Email + Push to doctor |
| Booking failed | Email + Push to patient |
| Cancellation by patient | Email + Push to doctor |
| Cancellation by doctor | Email + Push to patient |
| Reminder due | Push to patient |
| Consultation starting soon | Push to both |

Email templates are HTML files managed with **Thymeleaf**, allowing dynamic content injection (names, links, dates).

---

### 4.7 Video Calls

Live consultations use **Agora RTC** for real-time video.

#### Token generation flow

```
JobRunr job fires ~15 minutes before consultation start
  → ConsultationPreparationService.prepare(consultationId)
  → AgoraTokenService.generateToken(channelName, expiry)
      → channelName = consultation UUID (unique per session)
      → expiry = seconds until consultation.endTime
  → Token stored in Consultation.accessToken
  → Consultation.status → LIVE
  → Patient and doctor retrieve the token via GET /api/v1/consultation/{id}
  → Frontend initializes Agora SDK with this token
```

The Agora token automatically expires when the consultation is scheduled to end, preventing the channel from being used after the session.

---

## 5. Database Design

The schema is managed entirely through **Flyway** with 43 versioned migration scripts. All tables share a common structure inherited from the `BaseEntity`:

### Common columns (all tables)

| Column | Type | Description |
|---|---|---|
| `id` | UUID | Primary key, auto-generated |
| `created_at` | TIMESTAMPTZ | Set automatically on insert |
| `updated_at` | TIMESTAMPTZ | Updated automatically on each change |
| `created_by` | UUID | User ID who created the record |
| `updated_by` | UUID | User ID who last updated the record |
| `deleted` | BOOLEAN | Soft delete flag (default `false`) |
| `deleted_by` | VARCHAR | Who deleted the record |
| `deleted_at` | TIMESTAMPTZ | When the record was deleted |

All queries automatically apply the filter `WHERE deleted = false` via Hibernate's `@SQLRestriction`.

### Core entities and relationships

```
users (base)
  ├── doctor      (1:1 joined table)
  │     ├── consultation_slot    (1:N)
  │     │     └── consultation   (1:1 per slot)
  │     │           ├── payment  (1:1)
  │     │           └── consultation_summary (1:1)
  │     ├── education            (1:N)
  │     ├── experience           (1:N)
  │     └── feedback             (1:N)
  │
  └── patient     (1:1 joined table)
        ├── lab_test             (1:N)
        │     └── lab_result     (1:N)
        ├── medicine             (1:N)
        │     ├── group          (N:1)
        │     └── reminder       (1:N)
        │           └── reminder_log (1:N)
        └── address              (1:N)

token (multi-purpose: JWT_BLACKLIST, REFRESH_TOKEN, OTP)
notification
cancellation_log
consultation_log
payout
refund_request
drug + drug_company + active_ingredient
```

### Key indexes

- `users`: email, phone, id
- `consultation_slot`: doctor_id, status, (start_time, end_time)
- `consultation`: patient_id, slot_id, status
- `medicine`: patient_id
- `reminder`: medicine_id, scheduled_time

### Full-text search

A PostgreSQL materialized view `doctor_search_view` is used for doctor search queries, combining specialization, name, rating, and location into a searchable projection. It uses `pg_trgm` for trigram-based fuzzy search.

---

## 6. Key Design Decisions

### 6.1 Pessimistic locking for concurrent booking

**Problem:** Two patients booking the same consultation slot at the same time could both succeed, causing a double-booking.

**Solution:** `ReservationService.reserve()` uses `SELECT ... FOR UPDATE` (pessimistic lock) on the slot row. The transaction holds the lock until committed. Concurrent requests queue up and wait for the lock. The `@Retryable` annotation retries up to 3 times on `PessimisticLockingFailureException` with 500ms delay.

```java
@Retryable(retryFor = {PessimisticLockingFailureException.class},
           maxAttempts = 3, backoff = @Backoff(delay = 500))
public String reserve(ReserveConsultationRequest request) {
    ConsultationSlot slot = slotRepository
        .findConsultationByIdWithLock(request.slotId()) // SELECT FOR UPDATE
        .orElseThrow(...);
    // ...
}
```

### 6.2 Post-commit event publishing

**Problem:** Publishing a RabbitMQ message inside a transaction means the message could be sent even if the transaction later rolls back, causing notifications for operations that never actually persisted.

**Solution:** All RabbitMQ publishes and WebSocket broadcasts are wrapped in `TransactionSynchronizationManager.registerSynchronization()` so they only fire `afterCommit()`.

```java
TransactionSynchronizationManager.registerSynchronization(
    new TransactionSynchronization() {
        @Override
        public void afterCommit() {
            notificationManager.publishSuccessfulReservationNotification(...);
        }
    });
```

### 6.3 Idempotency for mutating requests

**Problem:** Mobile clients on flaky networks may retry a request (e.g., book a consultation) after a timeout, even if the first request succeeded. This could create duplicate bookings.

**Solution:** A custom `@Idempotent` annotation + `IdempotencyFilter` checks an `Idempotency-Key` request header. The response is cached in Redis. If the same key is seen again, the cached response is returned immediately without re-executing the handler. If the request is still in-flight, a `409 Conflict` is returned.

### 6.4 Async payment and refund processing

**Problem:** Stripe API calls can be slow or fail. Blocking the HTTP response thread while waiting for Stripe is poor UX and wastes server resources.

**Solution:**
- **Payment:** Stripe's `PaymentIntent` model is used. The server creates the intent and returns a `clientSecret` to the frontend. The frontend completes payment with Stripe SDK directly. Stripe then calls the server's webhook to confirm success/failure.
- **Refunds:** Processed via a RabbitMQ consumer, decoupled from the cancellation HTTP response. If Stripe is temporarily down, the message remains in the queue and is retried.

### 6.5 Soft deletes everywhere

Every entity extends `BaseEntity` which has a `deleted` boolean column and `@SQLRestriction("deleted = false")`. Deleting any entity just sets `deleted = true` with an audit trail (who deleted it, when). This is important in a healthcare context where data must never be truly destroyed — it may be needed for legal or clinical record-keeping.

### 6.6 Provider pattern for payment gateways

`PaymentProviderService` is an interface with implementations per gateway (currently only `StripeService`). The correct provider is resolved at runtime via Spring's `ApplicationContext.getBean(providerName)`. This means adding a new payment provider (e.g., PayPal) requires only implementing the interface and registering the bean — no changes to the booking flow.

### 6.7 Inheritance strategy for users

Doctors and patients share a `users` base table with common fields (name, email, password, phone, birthDate). Domain-specific fields live in separate `doctor` and `patient` tables joined by foreign key. This is JPA's `InheritanceType.JOINED` strategy. It avoids null columns (no doctor columns on patient rows) while keeping a single login table.

### 6.8 AI lab report extraction

Rather than building a custom ML model, the system uses Google Gemini's multimodal capability to understand medical PDFs directly. The PDF is base64-encoded and sent with a structured prompt that specifies the exact JSON output format. This removes the need for a separate AI infrastructure and can handle varied lab report layouts that a fixed parser would struggle with.

---

## 7. Security Model

### Authentication

| Mechanism | Details |
|---|---|
| Token type | JWT (HS256), signed with a secret key |
| Token delivery | HttpOnly cookies (`jwt` and `refreshToken`) — inaccessible to JavaScript |
| Token lifetime | JWT: 1 hour, Refresh token: ~7 days (configurable) |
| Password storage | BCrypt, cost factor 10 |
| OTP | 6-digit code, 5 minute expiry, stored hashed in DB |

### Authorization

Role-based via Spring Security. Two roles exist: `DOCTOR` and `PATIENT`. Method-level security via `@PreAuthorize`:

```java
@PreAuthorize("hasRole('DOCTOR')")
public void addConsultationSlot(...) { ... }

@PreAuthorize("hasRole('PATIENT')")
public void reserveConsultation(...) { ... }
```

Resource-level ownership checks are done in service methods (e.g., a doctor can only edit their own slots).

### Rate limiting

Bucket4j applies rate limits at the API level to prevent brute-force attacks and abuse. The `RateLimitingFilter` runs before the JWT filter in the chain.

### Input validation

Spring Validation (`@Valid`) on all request DTOs. Custom validators exist for:
- `@ValidAge` — ensures patient age is within a valid range
- `@FutureDate` — ensures consultation slots are not in the past
- `@YearNotAfterCurrent` — validates education/experience year ranges

---

## 8. API Reference

Base URL: `http://localhost:8030/api/v1`  
Interactive docs: `http://localhost:8030/swagger-ui.html`

### Authentication

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| POST | `/auth/login` | Public | Email + password login |
| POST | `/auth/refresh` | Public | Refresh JWT using refresh token |
| POST | `/auth/verify` | Public | Verify email with OTP |
| POST | `/oauth2/google` | Public | Google OAuth2 login |
| POST | `/password/forget-password` | Public | Request password reset OTP |
| POST | `/password/change-password` | Public | Reset password with OTP |

### User management

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| POST | `/user/register/patient` | Public | Register a new patient |
| POST | `/user/register/doctor` | Public | Register a new doctor |
| GET | `/user/me` | Any | Get own profile |

### Consultation

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| POST | `/consultation/slot` | Doctor | Create an available time slot |
| PUT | `/consultation/slot/{id}` | Doctor | Edit a slot |
| GET | `/consultation/slot` | Doctor | List own slots |
| POST | `/consultation/reserve` | Patient | Reserve a slot (initiates payment) |
| DELETE | `/consultation/{id}` | Doctor / Patient | Cancel a consultation |
| GET | `/consultation` | Any | List consultations |
| GET | `/consultation/{id}/summary` | Doctor | Get consultation summary |
| POST | `/consultation/{id}/summary` | Doctor | Add consultation notes/summary |

### Lab Tests

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| POST | `/lab-tests` | Patient | Upload lab report PDF → AI extraction |
| GET | `/lab-tests` | Patient | List own lab tests |
| GET | `/lab-tests/{id}` | Patient | Get test with results |
| DELETE | `/lab-tests/{id}` | Patient | Delete a lab test |

### Medicine & Reminders

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| POST | `/medicine` | Patient | Add a medicine |
| GET | `/medicine` | Patient | List medicines (filterable) |
| PATCH | `/medicine/{id}` | Patient | Update medicine |
| POST | `/medicine/group` | Patient | Create a medicine group |
| POST | `/reminder` | Patient | Set a reminder for a medicine |
| GET | `/reminder` | Patient | List reminders |
| DELETE | `/reminder/{id}` | Patient | Delete a reminder |

### Doctors

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| GET | `/doctor` | Any | Search doctors (name, specialization, rating) |
| GET | `/doctor/{id}` | Any | Get doctor profile |
| GET | `/specialization` | Public | List all specializations |

### Payments

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| POST | `/stripe/webhook` | Stripe | Stripe payment webhook (no auth) |

### Notifications

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| GET | `/notification` | Any | Get own notifications |
| PATCH | `/notification/{id}/read` | Any | Mark notification as read |

---

## 9. Running the Project

### Prerequisites

- Java 17+
- Maven 3.6+
- PostgreSQL 15
- Redis
- RabbitMQ
- Tesseract OCR 5.x

### Step 1 — Clone and configure

```bash
git clone https://github.com/Rafiq-Health-Care/rafiq-backend.git
cd rafiq-backend
```

Create `.env.properties` in the project root (see README for the full template). At minimum you need:

```properties
DATASOURCE_URL=jdbc:postgresql://localhost:5432/rafiq
DATASOURCE_USERNAME=postgres
DATASOURCE_PASSWORD=your_password
JWT_SECRET=your_base64_encoded_secret_min_32_chars
JWT_EXPIRATION=3600000
REFRESH_EXPIRATION=600000000
GOOGLE_CLIENT_ID=...
CLOUDINARY_CLOUD_NAME=...
CLOUDINARY_API_KEY=...
CLOUDINARY_API_SECRET=...
GOOGLE_GENAI_API_KEY=...
STRIPE_API_SECRET=...
STRIPE_WEBHOOK_SECRET=...
AGORA_APP_ID=...
AGORA_APP_CERTIFICATE=...
RABBITMQ_HOST=localhost
RABBITMQ_PORT=5672
RABBITMQ_USERNAME=guest
RABBITMQ_PASSWORD=guest
RABBITMQ_VIRTUAL_HOST=/
REDIS_HOST=localhost
REDIS_PORT=6379
FIREBASE_CONFIG=<base64 encoded firebase service account JSON>
SENTRY_DSN=...
```

### Step 2 — Create the database

```sql
CREATE DATABASE rafiq;
```

### Step 3 — Build and run

```bash
./mvnw clean install -DskipTests
./mvnw spring-boot:run
```

The app starts on port **8030**. Flyway runs all 43 migrations automatically on startup.

Access Swagger UI at: `http://localhost:8030/swagger-ui.html`  
Access JobRunr Dashboard at: `http://localhost:4040`

### Running tests

```bash
# Unit tests only
./mvnw test

# All tests including integration (requires Docker for Testcontainers)
./mvnw verify
```

### Docker

A `Dockerfile` is included. Build and run:

```bash
docker build -t rafiq-backend .
docker run -p 8030:8030 --env-file .env.properties rafiq-backend
```

---

## 10. Project Structure

```
rafiq-backend/
├── src/
│   ├── main/
│   │   ├── java/com/nexaworks/rafiq/
│   │   │   ├── config/           # Spring configuration beans
│   │   │   │   ├── ProjectConfig.java      # Auth manager, password encoder, UserDetailsService
│   │   │   │   ├── SecurityConfig.java     # HTTP security, filter chain, CORS
│   │   │   │   ├── RedisConfig.java        # Redis connection and template
│   │   │   │   ├── WebSocketConfig.java    # STOMP WebSocket endpoints
│   │   │   │   ├── AIConfig.java           # Gemini API client
│   │   │   │   ├── CloudinaryConfig.java   # Cloudinary SDK setup
│   │   │   │   ├── StripeConfig.java       # Stripe API key setup
│   │   │   │   └── FirebaseConfig.java     # Firebase Admin SDK
│   │   │   │
│   │   │   ├── controller/       # REST controllers (17 controllers)
│   │   │   ├── dto/              # Request/Response DTOs and event objects
│   │   │   ├── entities/         # JPA entities + enums
│   │   │   ├── exception/        # Custom exceptions + per-domain handlers
│   │   │   ├── idempotency/      # Idempotency filter, annotation, Redis/in-memory store
│   │   │   ├── mapper/           # MapStruct mappers (entity ↔ DTO)
│   │   │   ├── rabbit/           # RabbitMQ config, publishers, consumers, DLQ processors
│   │   │   ├── repository/       # Spring Data JPA repositories + Specification classes
│   │   │   ├── scheduler/        # JobRunr job definitions (expiry, preparation, payouts)
│   │   │   ├── security/         # JWT filter, rate limit filter, auth handlers
│   │   │   ├── service/          # Business logic, organized by domain:
│   │   │   │   ├── ai/           #   Gemini integration
│   │   │   │   ├── authentication/#  JWT, Auth, OAuth2
│   │   │   │   ├── consultation/ #   Slot, reservation, cancellation, preparation, call
│   │   │   │   ├── doctor/       #   Doctor profile, account management
│   │   │   │   ├── file/         #   Cloudinary, PDF extraction
│   │   │   │   ├── lab*/         #   Lab and lab test services
│   │   │   │   ├── medicine/     #   Medicine, group, reminder, drug
│   │   │   │   ├── notification/ #   Email content, push, persistence
│   │   │   │   ├── patient/      #   Patient profile
│   │   │   │   ├── payment/      #   Payment, Stripe, payout, refund
│   │   │   │   └── user/         #   User, password, token, role, address
│   │   │   ├── utils/            # AuthSessionManager, Prompt constants, TransactionUtils
│   │   │   └── validation/       # Custom annotation validators
│   │   │
│   │   └── resources/
│   │       ├── application.yml         # All configuration (env var references)
│   │       ├── db/migration/           # 43 Flyway SQL migration scripts
│   │       └── templates/              # Thymeleaf email HTML templates
│   │
│   └── test/
│       ├── unit/service/               # Unit tests (Mockito) for key services
│       └── integration/controller/     # Integration tests (Testcontainers + MockMvc)
│
├── api/                # Sample .http request files (for testing with IntelliJ/VS Code)
├── openapi/            # Generated OpenAPI spec (JSON + YAML)
├── Dockerfile
├── pom.xml
└── README.md
```

---
