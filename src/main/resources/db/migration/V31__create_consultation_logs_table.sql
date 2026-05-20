CREATE TABLE consultation_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    consultation_id UUID NOT NULL,
    doctor_enter_time TIMESTAMP,
    doctor_leave_time TIMESTAMP,
    patient_enter_time TIMESTAMP,
    patient_leave_time TIMESTAMP,
    CONSTRAINT uq_consultation_logs_consultation UNIQUE (consultation_id),
    CONSTRAINT fk_consultation_logs_consultation FOREIGN KEY (consultation_id) REFERENCES consultation (id)
);
