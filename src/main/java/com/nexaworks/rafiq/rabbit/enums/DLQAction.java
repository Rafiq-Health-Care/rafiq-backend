package com.nexaworks.rafiq.rabbit.enums;

public enum DLQAction {
    REDRIVE, // Transient failure, needs manual redrive after infra fix
    FIX_AND_DISCARD, // Bad data, fix side effect in DB then drop
    DISCARD, // Stale/expired, just drop
    ALERT // Critical issue, wake someone up
}
