package com.nexaworks.rafiq.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Action {
    DELETE("delete"), MOVE_TO_GROUP("moveToGroup"), MARK_ACTIVE("markActive"), MARK_INACTIVE(
            "markInActive");

    private final String action;
}
