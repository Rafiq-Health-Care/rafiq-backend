ALTER TABLE payments
    ADD CONSTRAINT fk_payments_refund_request
        FOREIGN KEY (refund_request_id) REFERENCES refund_request (id);
