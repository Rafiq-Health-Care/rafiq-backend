package com.nexaworks.rafiq.exception.custom.medicine;

public class MedicineAlreadyExist extends RuntimeException {
    public static final String CODE = "M03";

    public MedicineAlreadyExist(String message) {
        super(message);
    }
}
