package com.nexaworks.rafiq.exception.custom.medicine;

public class MedicineLimit extends RuntimeException {
    public static final String CODE = "M04";

    public MedicineLimit(String message) {
        super(message);
    }
}
