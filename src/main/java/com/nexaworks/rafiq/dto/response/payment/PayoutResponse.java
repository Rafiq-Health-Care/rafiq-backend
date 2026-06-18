package com.nexaworks.rafiq.dto.response.payment;

import com.nexaworks.rafiq.entities.enums.PayoutStatus;

public record PayoutResponse(PayoutStatus status, String transferId, String failureReason) {
}
