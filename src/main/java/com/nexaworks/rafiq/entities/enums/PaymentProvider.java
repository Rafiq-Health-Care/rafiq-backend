package com.nexaworks.rafiq.entities.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PaymentProvider {
    STRIPE("stripe");
    private final String name;
}
