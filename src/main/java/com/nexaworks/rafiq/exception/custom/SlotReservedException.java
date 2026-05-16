package com.nexaworks.rafiq.exception.custom;

public class SlotReservedException extends RuntimeException {
    public static final String CODE = "C04";
    public SlotReservedException(String consultationCannotBeReserved) {
    }
}
