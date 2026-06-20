package com.nexaworks.rafiq.exception.custom.medicine;

public class MedicineNotFound extends RuntimeException {
    public static final String CODE = "M05";

    public MedicineNotFound(String message) {
        super(message);
    }
}
