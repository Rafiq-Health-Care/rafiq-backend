package com.nexaworks.rafiq.exception.custom.consultation;

public class SlotCanNotCreated extends RuntimeException {
    public static final String CODE = "C01";
    public SlotCanNotCreated(String s) {
        super(s);
    }
}
