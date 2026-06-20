package com.nexaworks.rafiq.entities.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ConsultationStatus {
    PENDING, COMPLETED, CANCELLED, UPCOMING, LIVE
}
