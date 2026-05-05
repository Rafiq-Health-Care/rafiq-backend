package com.nexaworks.rafiq.entities.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ConsultationStatus {
    BOOKED(0.0),
    CONFIRMED(0.9),
    ONGOING(0.5),
    COMPLETED(0),
    CANCELLED(0),
    NO_SHOW(1),
    AVAILABLE(0),
    EXPIRED(0),
    RESCHEDULED(0);
    public boolean isTerminal() {
        return this == CANCELLED || this == COMPLETED;
    }
    private final double refundPercentage;


}
