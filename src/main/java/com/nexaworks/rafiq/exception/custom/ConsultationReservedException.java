package com.nexaworks.rafiq.exception.custom;

public class ConsultationReservedException extends RuntimeException {
    public static final String CODE = "C02";
    public ConsultationReservedException(String consultationCannotBeReserved) {
    }
}
