package com.nexaworks.rafiq.exception.custom.consultation;

public class SlotNotFoundException extends RuntimeException {
    public static final String CODE = "C03";
    public SlotNotFoundException(String consultationSlotNotFound) {
        super(consultationSlotNotFound);
    }
}
