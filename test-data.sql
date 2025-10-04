-- Test data SQL script for Rafiq backend (PostgreSQL)
-- This script inserts dummy data across all tables to help you test the database manually.
-- Safe to run multiple times: it clears data first using TRUNCATE ... CASCADE.
-- Run with: psql -h localhost -p 5460 -U postgres -d rafiq -f test-data.sql

BEGIN;

-- 1) Clean existing data
TRUNCATE TABLE 
  user_roles,
  address,
  medical_certifications,
  doctor_profile,
  specialization,
  users,
  role,
  patient_profile
RESTART IDENTITY CASCADE;

-- 2) Seed roles
-- Using explicit UUIDs for consistency
INSERT INTO role (id, name) VALUES
  ('00000000-0000-0000-0000-0000000000a1', 'ROLE_ADMIN'),
  ('00000000-0000-0000-0000-0000000000a2', 'ROLE_DOCTOR'),
  ('00000000-0000-0000-0000-0000000000a3', 'ROLE_PATIENT');

-- 3) Seed specializations (with audit fields)
INSERT INTO specialization (id, name, description, code, created_at, updated_at, created_by, updated_by) VALUES
  ('10000000-0000-0000-0000-000000000001', 'Cardiology', 'Heart and circulation', 'CARD', now(), now(), 'ffffffff-ffff-ffff-ffff-fffffffffff1', 'ffffffff-ffff-ffff-ffff-fffffffffff1'),
  ('10000000-0000-0000-0000-000000000002', 'Neurology', 'Brain and nervous system', 'NEUR', now(), now(), 'ffffffff-ffff-ffff-ffff-fffffffffff1', 'ffffffff-ffff-ffff-ffff-fffffffffff1'),
  ('10000000-0000-0000-0000-000000000003', 'Dermatology', 'Skin related', 'DERM', now(), now(), 'ffffffff-ffff-ffff-ffff-fffffffffff1', 'ffffffff-ffff-ffff-ffff-fffffffffff1');

-- 4) Seed a few users (with audit fields)
-- Note: password here is plain for demo; in real data Spring Security stores hashes.
INSERT INTO users (
  id, email, password, first_name, last_name, phone, age, active, locked, enabled, gender, doctor_profile_id,
  created_at, updated_at, created_by, updated_by
) VALUES
  ('20000000-0000-0000-0000-000000000001', 'admin@rafiq.local', '{noop}admin', 'Rafiq', 'Admin', '+201000000001', 30, true, false, true, 'MALE', NULL, now(), now(), 'ffffffff-ffff-ffff-ffff-fffffffffff1', 'ffffffff-ffff-ffff-ffff-fffffffffff1'),
  ('20000000-0000-0000-0000-000000000002', 'doc1@rafiq.local', '{noop}doctor', 'Doha', 'Doc', '+201000000002', 38, true, false, true, 'FEMALE', NULL, now(), now(), 'ffffffff-ffff-ffff-ffff-fffffffffff1', 'ffffffff-ffff-ffff-ffff-fffffffffff1'),
  ('20000000-0000-0000-0000-000000000003', 'pat1@rafiq.local', '{noop}patient', 'Peter', 'Patient', '+201000000003', 25, true, false, true, 'MALE', NULL, now(), now(), 'ffffffff-ffff-ffff-ffff-fffffffffff1', 'ffffffff-ffff-ffff-ffff-fffffffffff1');

-- 5) Assign roles to users via join table user_roles
INSERT INTO user_roles (user_id, role_id) VALUES
  ('20000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-0000000000a1'), -- admin -> ROLE_ADMIN
  ('20000000-0000-0000-0000-000000000002', '00000000-0000-0000-0000-0000000000a2'), -- doc1 -> ROLE_DOCTOR
  ('20000000-0000-0000-0000-000000000003', '00000000-0000-0000-0000-0000000000a3'); -- pat1 -> ROLE_PATIENT

-- 6) Create a doctor profile and link to user doc1
INSERT INTO doctor_profile (
  id, description, hospital_name, personal_photo, national_id, hospital_id, specialization_id,
  created_at, updated_at, created_by, updated_by
) VALUES (
  '30000000-0000-0000-0000-000000000001', 'Senior cardiologist with 10+ years experience', 'Cairo General Hospital',
  'https://example.com/photos/doc1.jpg', 'NAT-1234567890', 'HOS-987654321', '10000000-0000-0000-0000-000000000001',
  now(), now(), 'ffffffff-ffff-ffff-ffff-fffffffffff1', 'ffffffff-ffff-ffff-ffff-fffffffffff1'
);

-- Link doctor_profile to the doctor user
UPDATE users SET doctor_profile_id = '30000000-0000-0000-0000-000000000001' WHERE id = '20000000-0000-0000-0000-000000000002';

-- 7) Add medical certifications for the doctor
INSERT INTO medical_certifications (
  id, name, description, code, photo, doctor_id,
  created_at, updated_at, created_by, updated_by
) VALUES
  ('40000000-0000-0000-0000-000000000001', 'USMLE', 'United States Medical Licensing Examination', 'USMLE', 'https://example.com/certs/usmle.png', '30000000-0000-0000-0000-000000000001', now(), now(), 'ffffffff-ffff-ffff-ffff-fffffffffff1', 'ffffffff-ffff-ffff-ffff-fffffffffff1'),
  ('40000000-0000-0000-0000-000000000002', 'FACC', 'Fellow of the American College of Cardiology', 'FACC', 'https://example.com/certs/facc.png', '30000000-0000-0000-0000-000000000001', now(), now(), 'ffffffff-ffff-ffff-ffff-fffffffffff1', 'ffffffff-ffff-ffff-ffff-fffffffffff1');

-- 8) Addresses for users (required user_id)
INSERT INTO address (
  id, street, city, state, country, postal_code, user_id,
  created_at, updated_at, created_by, updated_by
) VALUES
  ('50000000-0000-0000-0000-000000000001', '12 Nile St', 'Giza', 'Giza', 'EG', '12511', '20000000-0000-0000-0000-000000000001', now(), now(), 'ffffffff-ffff-ffff-ffff-fffffffffff1', 'ffffffff-ffff-ffff-ffff-fffffffffff1'),
  ('50000000-0000-0000-0000-000000000002', '34 Tahrir Sq', 'Cairo', 'Cairo', 'EG', '11511', '20000000-0000-0000-0000-000000000002', now(), now(), 'ffffffff-ffff-ffff-ffff-fffffffffff1', 'ffffffff-ffff-ffff-ffff-fffffffffff1'),
  ('50000000-0000-0000-0000-000000000003', '56 Corniche', 'Alexandria', 'Alex', 'EG', '21511', '20000000-0000-0000-0000-000000000003', now(), now(), 'ffffffff-ffff-ffff-ffff-fffffffffff1', 'ffffffff-ffff-ffff-ffff-fffffffffff1');

-- 9) Patient profile (standalone entity currently)
INSERT INTO patient_profile (
  id, description,
  created_at, updated_at, created_by, updated_by
) VALUES
  ('60000000-0000-0000-0000-000000000001', 'Has mild seasonal allergies', now(), now(), 'ffffffff-ffff-ffff-ffff-fffffffffff1', 'ffffffff-ffff-ffff-ffff-fffffffffff1');

COMMIT;

-- Quick sanity checks
-- SELECT u.id, u.email, string_agg(r.name, ',') roles FROM users u LEFT JOIN user_roles ur ON u.id=ur.user_id LEFT JOIN role r ON r.id=ur.role_id GROUP BY u.id, u.email;
-- SELECT d.id, d.hospital_name, s.name specialization FROM doctor_profile d JOIN specialization s ON d.specialization_id=s.id;
-- SELECT m.name, d.hospital_name FROM medical_certifications m JOIN doctor_profile d ON m.doctor_id=d.id;
-- SELECT a.city, u.email FROM address a JOIN users u ON a.user_id=u.id;