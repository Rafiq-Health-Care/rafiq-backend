-- =============================================================================
-- V1 · Initial schema
-- Aligned 1-to-1 with JPA entities:
--   · BaseEntity  → soft-delete + audit columns on every table
--   · User / Doctor / Patient → JOINED inheritance (dtype discriminator)
--   · TimeSlot    → @Embeddable flattened into consultation columns
--   · WeightHistory → standalone entity (no BaseEntity)
-- =============================================================================

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pg_trgm";

-- =============================================================================
-- LOOKUP / REFERENCE TABLES
-- =============================================================================

-- ---------------------------------------------------------------------------
-- role
-- ---------------------------------------------------------------------------
CREATE TABLE role (
    id          UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    name        VARCHAR(100),
    -- BaseEntity --
    created_at  TIMESTAMPTZ  NOT NULL,
    updated_at  TIMESTAMPTZ,
    created_by  UUID         NOT NULL,
    updated_by  UUID,
    deleted     BOOLEAN      NOT NULL DEFAULT FALSE,
    deleted_by  VARCHAR(255),
    deleted_at  TIMESTAMPTZ
);

-- ---------------------------------------------------------------------------
-- social_links
-- ---------------------------------------------------------------------------
CREATE TABLE social_links (
    id          UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    facebook    VARCHAR(255),
    twitter     VARCHAR(255),
    instagram   VARCHAR(255),
    linkedin    VARCHAR(255),
    youtube     VARCHAR(255),
    whatsapp    VARCHAR(255),
    website     VARCHAR(255),
    -- BaseEntity --
    created_at  TIMESTAMPTZ  NOT NULL,
    updated_at  TIMESTAMPTZ,
    created_by  UUID         NOT NULL,
    updated_by  UUID,
    deleted     BOOLEAN      NOT NULL DEFAULT FALSE,
    deleted_by  VARCHAR(255),
    deleted_at  TIMESTAMPTZ
);

-- ---------------------------------------------------------------------------
-- company
-- ---------------------------------------------------------------------------
CREATE TABLE company (
    id          UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    name        VARCHAR(255),
    country     VARCHAR(255),
    description VARCHAR(255),
    -- BaseEntity --
    created_at  TIMESTAMPTZ  NOT NULL,
    updated_at  TIMESTAMPTZ,
    created_by  UUID         NOT NULL,
    updated_by  UUID,
    deleted     BOOLEAN      NOT NULL DEFAULT FALSE,
    deleted_by  VARCHAR(255),
    deleted_at  TIMESTAMPTZ
);

-- ---------------------------------------------------------------------------
-- active_ingredient
-- ---------------------------------------------------------------------------
CREATE TABLE active_ingredient (
    id          UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    name        VARCHAR(255),
    description VARCHAR(255),
    -- BaseEntity --
    created_at  TIMESTAMPTZ  NOT NULL,
    updated_at  TIMESTAMPTZ,
    created_by  UUID         NOT NULL,
    updated_by  UUID,
    deleted     BOOLEAN      NOT NULL DEFAULT FALSE,
    deleted_by  VARCHAR(255),
    deleted_at  TIMESTAMPTZ
);

-- =============================================================================
-- DRUG CATALOGUE  (populated by V2 Java migration)
-- =============================================================================

CREATE TABLE drug (
    id            UUID             PRIMARY KEY DEFAULT gen_random_uuid(),
    trade_name    VARCHAR(255),
    drug_group    VARCHAR(255),
    dosage_form   VARCHAR(255),
    route         VARCHAR(255),
    pharmacology  TEXT,
    price         DOUBLE PRECISION,
    search_vector TSVECTOR,
    -- BaseEntity --
    created_at    TIMESTAMPTZ      NOT NULL,
    updated_at    TIMESTAMPTZ,
    created_by    UUID             NOT NULL,
    updated_by    UUID,
    deleted       BOOLEAN          NOT NULL DEFAULT FALSE,
    deleted_by    VARCHAR(255),
    deleted_at    TIMESTAMPTZ
);

CREATE INDEX idx_drug_search_vector ON drug USING GIN (search_vector);
CREATE INDEX idx_drug_trade_name_trgm ON drug USING GIN (trade_name gin_trgm_ops);

-- Auto-update search_vector on relevant column changes
CREATE OR REPLACE FUNCTION fn_drug_search_vector_update()
RETURNS TRIGGER LANGUAGE plpgsql AS $$
BEGIN
    NEW.search_vector :=
        setweight(to_tsvector('english', COALESCE(NEW.trade_name,   '')), 'A') ||
        setweight(to_tsvector('english', COALESCE(NEW.drug_group,   '')), 'B') ||
        setweight(to_tsvector('english', COALESCE(NEW.pharmacology, '')), 'C');
    RETURN NEW;
END;
$$;

CREATE TRIGGER trg_drug_search_vector
    BEFORE INSERT OR UPDATE OF trade_name, drug_group, pharmacology ON drug
    FOR EACH ROW EXECUTE FUNCTION fn_drug_search_vector_update();

-- Many-to-many join tables for Drug
CREATE TABLE drug_company (
    drug_id    UUID NOT NULL REFERENCES drug             (id) ON DELETE CASCADE,
    company_id UUID NOT NULL REFERENCES company          (id) ON DELETE CASCADE,
    CONSTRAINT pk_drug_company PRIMARY KEY (drug_id, company_id)
);

CREATE TABLE drug_active_ingredient (
    drug_id              UUID NOT NULL REFERENCES drug             (id) ON DELETE CASCADE,
    active_ingredient_id UUID NOT NULL REFERENCES active_ingredient(id) ON DELETE CASCADE,
    CONSTRAINT pk_drug_active_ingredient PRIMARY KEY (drug_id, active_ingredient_id)
);

-- =============================================================================
-- USER / DOCTOR / PATIENT  (JOINED inheritance, dtype discriminator)
-- =============================================================================

CREATE TABLE users (
    id                 UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    email              VARCHAR(255) NOT NULL,
    password           VARCHAR(255),
    first_name         VARCHAR(255) NOT NULL,
    last_name          VARCHAR(255),
    phone              VARCHAR(255),
    birth_date         DATE,
    active             BOOLEAN      NOT NULL DEFAULT TRUE,
    locked             BOOLEAN      NOT NULL DEFAULT FALSE,
    enabled            BOOLEAN      NOT NULL DEFAULT FALSE,
    notification_token VARCHAR(255),
    gender             VARCHAR(50),
    -- BaseEntity --
    created_at         TIMESTAMPTZ  NOT NULL,
    updated_at         TIMESTAMPTZ,
    created_by         UUID         NOT NULL,
    updated_by         UUID,
    deleted            BOOLEAN      NOT NULL DEFAULT FALSE,
    deleted_by         VARCHAR(255),
    deleted_at         TIMESTAMPTZ,
    CONSTRAINT uq_users_email UNIQUE (email)
);

CREATE INDEX idx_users_email ON users (email);

-- Doctor subtype table — PK is a FK to users.id
CREATE TABLE doctor (
    id              UUID         PRIMARY KEY REFERENCES users (id) ON DELETE CASCADE,
    description     VARCHAR(255),
    hospital_name   VARCHAR(255),
    personal_photo  VARCHAR(255),
    national_id     VARCHAR(255),
    hospital_id     VARCHAR(255),
    -- Longest Specialization value: ORAL_MAXILLOFACIAL_SURGERY (26 chars)
    specialization  VARCHAR(100),
    social_links_id UUID         REFERENCES social_links (id) ON DELETE SET NULL,
    public_id       VARCHAR(255),
    -- Status enum: IN_REVIEW, VERIFIED, REJECTED
    status          VARCHAR(50)
);

CREATE INDEX idx_doctor_specialization ON doctor (specialization);

-- Patient subtype table — PK is a FK to users.id
CREATE TABLE patient (
    id                      UUID             PRIMARY KEY REFERENCES users (id) ON DELETE CASCADE,
    description             VARCHAR(255),
    height                  INTEGER          NOT NULL DEFAULT 0,
    weight                  DOUBLE PRECISION NOT NULL DEFAULT 0,
    -- BloodType enum: A_POSITIVE … O_NEGATIVE
    blood_type              VARCHAR(50),
    -- SmokeStatus enum: YES, NO, FORMER
    smoke_status            VARCHAR(50),
    cigarettes_per_day      INTEGER          NOT NULL DEFAULT 0,
    last_smoked             TIMESTAMPTZ,
    alcoholism              BOOLEAN          NOT NULL DEFAULT FALSE,
    drinks_per_week         INTEGER          NOT NULL DEFAULT 0,
    pregnant                BOOLEAN          NOT NULL DEFAULT FALSE,
    occupation              VARCHAR(255),
    emergency_contact_name  VARCHAR(255),
    emergency_contact_phone VARCHAR(255)
);

-- User ↔ Role  (many-to-many, @JoinTable name = user_roles)
CREATE TABLE user_roles (
    user_id UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    role_id UUID NOT NULL REFERENCES role  (id) ON DELETE CASCADE,
    CONSTRAINT pk_user_roles PRIMARY KEY (user_id, role_id)
);

-- =============================================================================
-- LAB / DIAGNOSTICS
-- =============================================================================

CREATE TABLE lab (
    id              UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    name            VARCHAR(255),
    logo            VARCHAR(255),
    public_id       VARCHAR(255),
    social_links_id UUID        REFERENCES social_links (id) ON DELETE SET NULL,
    -- BaseEntity --
    created_at      TIMESTAMPTZ NOT NULL,
    updated_at      TIMESTAMPTZ,
    created_by      UUID        NOT NULL,
    updated_by      UUID,
    deleted         BOOLEAN     NOT NULL DEFAULT FALSE,
    deleted_by      VARCHAR(255),
    deleted_at      TIMESTAMPTZ
);

CREATE TABLE lab_test (
    id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    name        VARCHAR(255),
    description VARCHAR(255),
    code        VARCHAR(255),
    pdf         VARCHAR(255),
    public_id   VARCHAR(255),
    file_type   VARCHAR(255),
    -- Instant maps to TIMESTAMPTZ
    date        TIMESTAMPTZ,
    lab_id      UUID        REFERENCES lab    (id) ON DELETE SET NULL,
    doctor_id   UUID        REFERENCES doctor (id) ON DELETE SET NULL,
    patient_id  UUID        REFERENCES patient(id) ON DELETE SET NULL,
    -- BaseEntity --
    created_at  TIMESTAMPTZ NOT NULL,
    updated_at  TIMESTAMPTZ,
    created_by  UUID        NOT NULL,
    updated_by  UUID,
    deleted     BOOLEAN     NOT NULL DEFAULT FALSE,
    deleted_by  VARCHAR(255),
    deleted_at  TIMESTAMPTZ
);

CREATE INDEX idx_lab_test_patient ON lab_test (patient_id);
CREATE INDEX idx_lab_test_doctor  ON lab_test (doctor_id);

CREATE TABLE lab_result (
    id            UUID             PRIMARY KEY DEFAULT gen_random_uuid(),
    name          VARCHAR(255),
    result        DOUBLE PRECISION,
    unit          VARCHAR(255),
    status        VARCHAR(255),
    description   VARCHAR(255),
    normal_result VARCHAR(255),
    lab_test_id   UUID             REFERENCES lab_test (id) ON DELETE CASCADE,
    -- BaseEntity --
    created_at    TIMESTAMPTZ      NOT NULL,
    updated_at    TIMESTAMPTZ,
    created_by    UUID             NOT NULL,
    updated_by    UUID,
    deleted       BOOLEAN          NOT NULL DEFAULT FALSE,
    deleted_by    VARCHAR(255),
    deleted_at    TIMESTAMPTZ
);

CREATE INDEX idx_lab_result_lab_test ON lab_result (lab_test_id);

-- =============================================================================
-- SHARED / SUPPORTING
-- =============================================================================

CREATE TABLE medical_certifications (
    id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    name        VARCHAR(255),
    description VARCHAR(255),
    code        VARCHAR(255),
    photo       VARCHAR(255),
    doctor_id   UUID        NOT NULL REFERENCES doctor (id) ON DELETE CASCADE,
    -- BaseEntity --
    created_at  TIMESTAMPTZ NOT NULL,
    updated_at  TIMESTAMPTZ,
    created_by  UUID        NOT NULL,
    updated_by  UUID,
    deleted     BOOLEAN     NOT NULL DEFAULT FALSE,
    deleted_by  VARCHAR(255),
    deleted_at  TIMESTAMPTZ
);

CREATE TABLE address (
    id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    street      VARCHAR(255),
    city        VARCHAR(255),
    state       VARCHAR(255),
    country     VARCHAR(255),
    postal_code VARCHAR(255),
    user_id     UUID        REFERENCES users (id) ON DELETE CASCADE,
    lab_id      UUID        REFERENCES lab   (id) ON DELETE CASCADE,
    -- BaseEntity --
    created_at  TIMESTAMPTZ NOT NULL,
    updated_at  TIMESTAMPTZ,
    created_by  UUID        NOT NULL,
    updated_by  UUID,
    deleted     BOOLEAN     NOT NULL DEFAULT FALSE,
    deleted_by  VARCHAR(255),
    deleted_at  TIMESTAMPTZ
);

CREATE TABLE token (
    id          UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    -- length 1000 matches @Column(length = 1000) on Token entity
    token       VARCHAR(1000) NOT NULL,
    token_type  VARCHAR(50),
    expiry_date TIMESTAMPTZ,
    user_id     UUID         NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    -- BaseEntity --
    created_at  TIMESTAMPTZ  NOT NULL,
    updated_at  TIMESTAMPTZ,
    created_by  UUID         NOT NULL,
    updated_by  UUID,
    deleted     BOOLEAN      NOT NULL DEFAULT FALSE,
    deleted_by  VARCHAR(255),
    deleted_at  TIMESTAMPTZ,
    CONSTRAINT uq_token UNIQUE (token)
);

CREATE INDEX idx_token_user ON token (user_id);

-- =============================================================================
-- MEDICINE / REMINDER  (patient-facing features)
-- =============================================================================

-- groups (@Table(name = "groups"))
CREATE TABLE groups (
    id             UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    name           VARCHAR(255),
    description    VARCHAR(255),
    icon_public_id VARCHAR(255),
    icon_url       VARCHAR(255),
    -- Color enum stored as string
    color          VARCHAR(50),
    patient_id     UUID        NOT NULL REFERENCES patient (id) ON DELETE CASCADE,
    -- BaseEntity --
    created_at     TIMESTAMPTZ NOT NULL,
    updated_at     TIMESTAMPTZ,
    created_by     UUID        NOT NULL,
    updated_by     UUID,
    deleted        BOOLEAN     NOT NULL DEFAULT FALSE,
    deleted_by     VARCHAR(255),
    deleted_at     TIMESTAMPTZ
);

CREATE INDEX idx_groups_patient ON groups (patient_id);

CREATE TABLE medicine (
    id               UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    name             VARCHAR(255),
    dosage           VARCHAR(255) NOT NULL,
    -- MedicineFrequency enum
    frequency        VARCHAR(50)  NOT NULL,
    -- ReminderFrequency enum (nullable)
    reminder_frequency VARCHAR(50),
    -- MedicineStatus enum, default ACTIVE
    status           VARCHAR(50)  NOT NULL DEFAULT 'ACTIVE',
    -- MedicineType enum (nullable)
    type             VARCHAR(50),
    -- Instant fields
    start_date       TIMESTAMPTZ,
    end_date         TIMESTAMPTZ,
    notes            TEXT,
    photo_url        VARCHAR(255),
    photo_public_id  VARCHAR(255),
    search_vector    TSVECTOR,
    drug_id          UUID         NOT NULL REFERENCES drug    (id),
    doctor_id        UUID         REFERENCES doctor  (id) ON DELETE SET NULL,
    patient_id       UUID         NOT NULL REFERENCES patient (id) ON DELETE CASCADE,
    group_id         UUID         REFERENCES groups  (id) ON DELETE SET NULL,
    -- BaseEntity --
    created_at       TIMESTAMPTZ  NOT NULL,
    updated_at       TIMESTAMPTZ,
    created_by       UUID         NOT NULL,
    updated_by       UUID,
    deleted          BOOLEAN      NOT NULL DEFAULT FALSE,
    deleted_by       VARCHAR(255),
    deleted_at       TIMESTAMPTZ
);

CREATE INDEX idx_medicine_search_vector ON medicine USING GIN (search_vector);
CREATE INDEX idx_medicine_name_trgm     ON medicine USING GIN (name gin_trgm_ops);
CREATE INDEX idx_medicine_patient       ON medicine (patient_id);
CREATE INDEX idx_medicine_doctor        ON medicine (doctor_id);

CREATE OR REPLACE FUNCTION fn_medicine_search_vector_update()
RETURNS TRIGGER LANGUAGE plpgsql AS $$
BEGIN
    NEW.search_vector :=
        setweight(to_tsvector('english', COALESCE(NEW.name,   '')), 'A') ||
        setweight(to_tsvector('english', COALESCE(NEW.notes,  '')), 'B') ||
        setweight(to_tsvector('english', COALESCE(NEW.dosage, '')), 'C');
    RETURN NEW;
END;
$$;

CREATE TRIGGER trg_medicine_search_vector
    BEFORE INSERT OR UPDATE OF name, notes, dosage ON medicine
    FOR EACH ROW EXECUTE FUNCTION fn_medicine_search_vector_update();

-- custom_days (@ElementCollection on Medicine.customDays,
--              @CollectionTable join column = "reminder_id")
CREATE TABLE custom_days (
    reminder_id UUID    NOT NULL REFERENCES medicine (id) ON DELETE CASCADE,
    day         VARCHAR(50)
);

CREATE INDEX idx_custom_days_reminder ON custom_days (reminder_id);

-- reminder (one-to-one with medicine)
CREATE TABLE reminder (
    id            UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    vibrate       BOOLEAN,
    -- ReminderStatus enum (nullable)
    status        VARCHAR(50),
    -- LocalDateTime → TIMESTAMP WITHOUT TIME ZONE
    next_reminder TIMESTAMP,
    disable       BOOLEAN     NOT NULL DEFAULT FALSE,
    -- UNIQUE enforces the OneToOne mapping
    medicine_id   UUID        NOT NULL REFERENCES medicine (id) ON DELETE CASCADE,
    patient_id    UUID        NOT NULL REFERENCES patient  (id) ON DELETE CASCADE,
    group_id      UUID        REFERENCES groups   (id) ON DELETE SET NULL,
    -- BaseEntity --
    created_at    TIMESTAMPTZ NOT NULL,
    updated_at    TIMESTAMPTZ,
    created_by    UUID        NOT NULL,
    updated_by    UUID,
    deleted       BOOLEAN     NOT NULL DEFAULT FALSE,
    deleted_by    VARCHAR(255),
    deleted_at    TIMESTAMPTZ,
    CONSTRAINT uq_reminder_medicine UNIQUE (medicine_id)
);

CREATE INDEX idx_reminder_patient  ON reminder (patient_id);
CREATE INDEX idx_reminder_medicine ON reminder (medicine_id);

-- reminder_log
CREATE TABLE reminder_log (
    id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    -- ReminderStatus enum (nullable)
    status      VARCHAR(50),
    -- LocalDateTime → TIMESTAMP WITHOUT TIME ZONE
    timestamp   TIMESTAMP,
    reminder_id UUID        NOT NULL REFERENCES reminder (id) ON DELETE CASCADE,
    patient_id  UUID        NOT NULL REFERENCES patient  (id) ON DELETE CASCADE,
    -- BaseEntity --
    created_at  TIMESTAMPTZ NOT NULL,
    updated_at  TIMESTAMPTZ,
    created_by  UUID        NOT NULL,
    updated_by  UUID,
    deleted     BOOLEAN     NOT NULL DEFAULT FALSE,
    deleted_by  VARCHAR(255),
    deleted_at  TIMESTAMPTZ
);

CREATE INDEX idx_reminder_log_reminder ON reminder_log (reminder_id);
CREATE INDEX idx_reminder_log_patient  ON reminder_log (patient_id);

-- weight_history (standalone entity — no BaseEntity, no soft-delete)
CREATE TABLE weight_history (
    id         UUID             PRIMARY KEY DEFAULT gen_random_uuid(),
    weight     DOUBLE PRECISION NOT NULL,
    date       DATE,
    patient_id UUID             NOT NULL REFERENCES patient (id) ON DELETE CASCADE
);

CREATE INDEX idx_weight_history_patient ON weight_history (patient_id);

-- =============================================================================
-- CONSULTATION
-- =============================================================================

CREATE TABLE consultation (
    id               UUID           PRIMARY KEY DEFAULT gen_random_uuid(),
    doctor_id        UUID           NOT NULL REFERENCES doctor  (id),
    patient_id       UUID           REFERENCES patient (id),
    -- TimeSlot @Embeddable fields (LocalDate / LocalTime / int)
    date             DATE           NOT NULL,
    start_time       TIME           NOT NULL,
    duration_minutes INTEGER        NOT NULL,
    end_time         TIME,
    -- ConsultationStatus enum
    status           VARCHAR(50)    NOT NULL DEFAULT 'AVAILABLE',
    notes            TEXT,
    price            NUMERIC(10, 2) NOT NULL,
    -- BaseEntity --
    created_at       TIMESTAMPTZ    NOT NULL,
    updated_at       TIMESTAMPTZ,
    created_by       UUID           NOT NULL,
    updated_by       UUID,
    deleted          BOOLEAN        NOT NULL DEFAULT FALSE,
    deleted_by       VARCHAR(255),
    deleted_at       TIMESTAMPTZ,
    -- Matches @UniqueConstraint(name = "uk_doctor_date_start", ...)
    CONSTRAINT uk_doctor_date_start UNIQUE (doctor_id, date, start_time)
);

CREATE INDEX idx_consultation_doctor  ON consultation (doctor_id);
CREATE INDEX idx_consultation_patient ON consultation (patient_id);
CREATE INDEX idx_consultation_status  ON consultation (status);

-- cancellation_log (OneToOne with Consultation — unique FK)
CREATE TABLE cancellation_log (
    id              UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    -- UNIQUE enforces the OneToOne mapping from Consultation side
    consultation_id UUID        NOT NULL REFERENCES consultation (id) ON DELETE CASCADE,
    reason          TEXT        NOT NULL,
    cancelled_by    UUID        NOT NULL REFERENCES users (id),
    -- BaseEntity --
    created_at      TIMESTAMPTZ NOT NULL,
    updated_at      TIMESTAMPTZ,
    created_by      UUID        NOT NULL,
    updated_by      UUID,
    deleted         BOOLEAN     NOT NULL DEFAULT FALSE,
    deleted_by      VARCHAR(255),
    deleted_at      TIMESTAMPTZ,
    CONSTRAINT uq_cancellation_log_consultation UNIQUE (consultation_id)
);
