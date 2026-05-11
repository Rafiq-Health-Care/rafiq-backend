package com.nexaworks.rafiq.entities.enums;

import java.time.LocalDate;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Availability {
    ANYTIME(LocalDate.now().plusYears(100)), TODAY(LocalDate.now()), NEXT_THREE_DAYS(
            LocalDate.now().plusDays(3)), THIS_WEEK(LocalDate.now().plusWeeks(1)),;
    private final LocalDate dateTime;

}
