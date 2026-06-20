package com.nexaworks.rafiq.exception.custom.consultation;

public class SlotCanNotEditException extends RuntimeException {
    public static final String CODE = "C02";
    public SlotCanNotEditException(String s) {
        super(s);
    }
}
