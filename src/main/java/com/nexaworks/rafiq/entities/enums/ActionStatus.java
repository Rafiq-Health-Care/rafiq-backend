package com.nexaworks.rafiq.entities.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ActionStatus {
    CONSULTATION_CANCELLED("Consultation Cancelled"), CONSULTATION_COMING_UP(
            "Consultation Coming Up"), REFUND_SUCCESS("Refund Successful"), REFUND_FAILED(
                    "Refund Failed"), NEW_CONSULTATION(
                            "New Consultation"), CONSULTATION_FAILED("Consultation Failed"),;
    private final String title;
}
