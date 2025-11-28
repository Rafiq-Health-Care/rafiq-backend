package com.nexaworks.rafiq.entities.enums;

import lombok.Getter;

@Getter

public enum MedicineFrequency {
    ONCE(1), TWICE(2), THIRD_TIMES(3), FOUR_TIMES(4), FIVE_TIMES(5), SIX_TIMES(6), SEVEN_TIMES(
            7), EIGHT_TIMES(8), NINE_TIMES(
                    9), TEN_TIMES(10), ELEVEN_TIMES(11), TWELVE_TIMES(12), AS_NEEDED, CUSTOM;
    private final int numberOfTimes;
    MedicineFrequency() {
        this.numberOfTimes = 0;
    }
    MedicineFrequency(int numberOfTimes) {
        this.numberOfTimes = numberOfTimes;
    }

}
