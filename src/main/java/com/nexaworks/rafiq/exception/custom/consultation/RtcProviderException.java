package com.nexaworks.rafiq.exception.custom.consultation;

public class RtcProviderException extends RuntimeException {
    public static final String CODE = "R02";
    public RtcProviderException(String message) {
        super(message);
    }
}
