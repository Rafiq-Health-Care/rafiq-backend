-- Create PostgreSQL extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS pg_trgm;

-- Create all schemas for test database
CREATE SCHEMA IF NOT EXISTS user_schema;
CREATE SCHEMA IF NOT EXISTS doctor_schema;
CREATE SCHEMA IF NOT EXISTS patient_schema;
CREATE SCHEMA IF NOT EXISTS medication_schema;
CREATE SCHEMA IF NOT EXISTS lab_schema;
CREATE SCHEMA IF NOT EXISTS lab_test_schema;
CREATE SCHEMA IF NOT EXISTS file_managment_schema;
