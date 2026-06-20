ALTER TABLE doctor ADD COLUMN stripe_customer_id VARCHAR(255);
ALTER TABLE doctor ADD CONSTRAINT doctor_stripe_customer_id_unique UNIQUE (stripe_customer_id);