package com.nexaworks.rafiq.unit.secheduler.service;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.nexaworks.rafiq.entities.enums.Day;
import com.nexaworks.rafiq.entities.enums.MedicineFrequency;
import com.nexaworks.rafiq.entities.enums.ReminderFrequency;
import com.nexaworks.rafiq.secheduler.service.serviceImpl.CornExpressionBuilderImpl;

@DisplayName("CornExpressionBuilder Unit Tests")
class CornExpressionBuilderImplTest {

    private CornExpressionBuilderImpl cornExpressionBuilder;
    private Instant startDate;

    @BeforeEach
    void setUp() {
        cornExpressionBuilder = new CornExpressionBuilderImpl();
        // Set start date to a specific time: 2025-11-25 08:30:00
        LocalDateTime dateTime = LocalDateTime.of(2025, 11, 25, 8, 30);
        startDate = dateTime.atZone(ZoneId.systemDefault()).toInstant();
    }

    @Nested
    @DisplayName("Daily Cron Expression Tests")
    class DailyCronExpressionTests {

        @Test
        @DisplayName("Should build daily cron for ONCE frequency")
        void shouldBuildDailyCronForOnce() {
            String result = cornExpressionBuilder.buildCornExpression(MedicineFrequency.ONCE,
                    ReminderFrequency.DAILY, null, startDate);

            assertEquals("0 30 8 * * ?", result);
        }

        @Test
        @DisplayName("Should build daily cron for TWICE frequency")
        void shouldBuildDailyCronForTwice() {
            String result = cornExpressionBuilder.buildCornExpression(MedicineFrequency.TWICE,
                    ReminderFrequency.DAILY, null, startDate);

            assertEquals("0 30 8,20 * * ?", result);
        }

        @Test
        @DisplayName("Should build daily cron for THIRD_TIMES frequency")
        void shouldBuildDailyCronForThirdTimes() {
            String result = cornExpressionBuilder.buildCornExpression(MedicineFrequency.THIRD_TIMES,
                    ReminderFrequency.DAILY, null, startDate);

            assertEquals("0 30 8,16,0 * * ?", result);
        }

        @Test
        @DisplayName("Should build daily cron for FOUR_TIMES frequency")
        void shouldBuildDailyCronForFourTimes() {
            String result = cornExpressionBuilder.buildCornExpression(MedicineFrequency.FOUR_TIMES,
                    ReminderFrequency.DAILY, null, startDate);

            assertEquals("0 30 8,14,20,2 * * ?", result);
        }
    }

    @Nested
    @DisplayName("Weekly Cron Expression Tests")
    class WeeklyCronExpressionTests {

        @Test
        @DisplayName("Should build weekly cron for ONCE frequency")
        void shouldBuildWeeklyCronForOnce() {
            String result = cornExpressionBuilder.buildCornExpression(MedicineFrequency.ONCE,
                    ReminderFrequency.WEEKLY, null, startDate);

            // startDate is Tuesday (2025-11-25), so dayOfWeek = 2
            assertEquals("0 30 8 ? * 2", result);
        }

        @Test
        @DisplayName("Should build weekly cron for TWICE frequency")
        void shouldBuildWeeklyCronForTwice() {
            String result = cornExpressionBuilder.buildCornExpression(MedicineFrequency.TWICE,
                    ReminderFrequency.WEEKLY, null, startDate);

            // Should trigger on Tuesday and Friday (2 and 5)
            assertEquals("0 30 8 ? * 2,5", result);
        }

        @Test
        @DisplayName("Should build weekly cron for THIRD_TIMES frequency")
        void shouldBuildWeeklyCronForThirdTimes() {
            String result = cornExpressionBuilder.buildCornExpression(MedicineFrequency.THIRD_TIMES,
                    ReminderFrequency.WEEKLY, null, startDate);

            // Should trigger on Tuesday, Thursday, Saturday (2, 4, 6)
            assertEquals("0 30 8 ? * 2,4,6", result);
        }
    }

    @Nested
    @DisplayName("Monthly Cron Expression Tests")
    class MonthlyCronExpressionTests {

        @Test
        @DisplayName("Should build monthly cron for ONCE frequency")
        void shouldBuildMonthlyCronForOnce() {
            String result = cornExpressionBuilder.buildCornExpression(MedicineFrequency.ONCE,
                    ReminderFrequency.MONTHLY, null, startDate);

            assertEquals("0 30 8 1 * ?", result);
        }

        @Test
        @DisplayName("Should build monthly cron for TWICE frequency")
        void shouldBuildMonthlyCronForTwice() {
            String result = cornExpressionBuilder.buildCornExpression(MedicineFrequency.TWICE,
                    ReminderFrequency.MONTHLY, null, startDate);

            assertEquals("0 30 8 1,16 * ?", result);
        }

        @Test
        @DisplayName("Should build monthly cron for THIRD_TIMES frequency")
        void shouldBuildMonthlyCronForThirdTimes() {
            String result = cornExpressionBuilder.buildCornExpression(MedicineFrequency.THIRD_TIMES,
                    ReminderFrequency.MONTHLY, null, startDate);

            assertEquals("0 30 8 1,11,21 * ?", result);
        }
    }

    @Nested
    @DisplayName("Yearly Cron Expression Tests")
    class YearlyCronExpressionTests {

        @Test
        @DisplayName("Should build yearly cron for ONCE frequency")
        void shouldBuildYearlyCronForOnce() {
            String result = cornExpressionBuilder.buildCornExpression(MedicineFrequency.ONCE,
                    ReminderFrequency.YEARLY, null, startDate);

            // startDate is Tuesday, dayOfWeek = 2
            assertEquals("0 30 8 2 * ? 1", result);
        }

        @Test
        @DisplayName("Should build yearly cron for TWICE frequency")
        void shouldBuildYearlyCronForTwice() {
            String result = cornExpressionBuilder.buildCornExpression(MedicineFrequency.TWICE,
                    ReminderFrequency.YEARLY, null, startDate);

            // Should trigger in January and July (months 1 and 7)
            assertEquals("0 30 8 2 * ? 1,7", result);
        }

        @Test
        @DisplayName("Should build yearly cron for THIRD_TIMES frequency")
        void shouldBuildYearlyCronForThirdTimes() {
            String result = cornExpressionBuilder.buildCornExpression(MedicineFrequency.THIRD_TIMES,
                    ReminderFrequency.YEARLY, null, startDate);

            // Should trigger in January, May, September (months 1, 5, 9)
            assertEquals("0 30 8 2 * ? 1,5,9", result);
        }

        @Test
        @DisplayName("Should build yearly cron for FOUR_TIMES frequency")
        void shouldBuildYearlyCronForFourTimes() {
            String result = cornExpressionBuilder.buildCornExpression(MedicineFrequency.FOUR_TIMES,
                    ReminderFrequency.YEARLY, null, startDate);

            // Should trigger in January, April, July, October (months 1, 4, 7, 10)
            assertEquals("0 30 8 2 * ? 1,4,7,10", result);
        }
    }

    @Nested
    @DisplayName("Custom Cron Expression Tests")
    class CustomCronExpressionTests {

        @Test
        @DisplayName("Should build custom cron for single day")
        void shouldBuildCustomCronForSingleDay() {
            List<Day> customDays = List.of(Day.MONDAY);

            String result = cornExpressionBuilder.buildCornExpression(MedicineFrequency.ONCE,
                    ReminderFrequency.CUSTOM, customDays, startDate);

            assertEquals("0 30 8 ? * 1", result);
        }

        @Test
        @DisplayName("Should build custom cron for multiple days")
        void shouldBuildCustomCronForMultipleDays() {
            List<Day> customDays = List.of(Day.MONDAY, Day.WEDNESDAY, Day.FRIDAY);

            String result = cornExpressionBuilder.buildCornExpression(MedicineFrequency.ONCE,
                    ReminderFrequency.CUSTOM, customDays, startDate);

            assertEquals("0 30 8 ? * 1,3,5", result);
        }

        @Test
        @DisplayName("Should build custom cron for weekdays")
        void shouldBuildCustomCronForWeekdays() {
            List<Day> customDays = List.of(Day.MONDAY, Day.TUESDAY, Day.WEDNESDAY, Day.THURSDAY,
                    Day.FRIDAY);

            String result = cornExpressionBuilder.buildCornExpression(MedicineFrequency.ONCE,
                    ReminderFrequency.CUSTOM, customDays, startDate);

            assertEquals("0 30 8 ? * 1,2,3,4,5", result);
        }

        @Test
        @DisplayName("Should build custom cron for weekend")
        void shouldBuildCustomCronForWeekend() {
            List<Day> customDays = List.of(Day.SATURDAY, Day.SUNDAY);

            String result = cornExpressionBuilder.buildCornExpression(MedicineFrequency.ONCE,
                    ReminderFrequency.CUSTOM, customDays, startDate);

            assertEquals("0 30 8 ? * 6,7", result);
        }

        @Test
        @DisplayName("Should build custom cron for all days")
        void shouldBuildCustomCronForAllDays() {
            List<Day> customDays = List.of(Day.MONDAY, Day.TUESDAY, Day.WEDNESDAY, Day.THURSDAY,
                    Day.FRIDAY, Day.SATURDAY, Day.SUNDAY);

            String result = cornExpressionBuilder.buildCornExpression(MedicineFrequency.ONCE,
                    ReminderFrequency.CUSTOM, customDays, startDate);

            assertEquals("0 30 8 ? * 1,2,3,4,5,6,7", result);
        }
    }

    @Nested
    @DisplayName("Different Start Time Tests")
    class DifferentStartTimeTests {

        @Test
        @DisplayName("Should build cron with different hour and minute")
        void shouldBuildCronWithDifferentTime() {
            // Set start date to 14:45
            LocalDateTime dateTime = LocalDateTime.of(2025, 11, 25, 14, 45);
            Instant customStartDate = dateTime.atZone(ZoneId.systemDefault()).toInstant();

            String result = cornExpressionBuilder.buildCornExpression(MedicineFrequency.ONCE,
                    ReminderFrequency.DAILY, null, customStartDate);

            assertEquals("0 45 14 * * ?", result);
        }

        @Test
        @DisplayName("Should build cron with midnight start time")
        void shouldBuildCronWithMidnight() {
            // Set start date to 00:00
            LocalDateTime dateTime = LocalDateTime.of(2025, 11, 25, 0, 0);
            Instant customStartDate = dateTime.atZone(ZoneId.systemDefault()).toInstant();

            String result = cornExpressionBuilder.buildCornExpression(MedicineFrequency.TWICE,
                    ReminderFrequency.DAILY, null, customStartDate);

            assertEquals("0 0 0,12 * * ?", result);
        }

        @Test
        @DisplayName("Should handle hour overflow correctly")
        void shouldHandleHourOverflow() {
            // Set start date to 20:00, with THIRD_TIMES it should wrap around
            LocalDateTime dateTime = LocalDateTime.of(2025, 11, 25, 20, 0);
            Instant customStartDate = dateTime.atZone(ZoneId.systemDefault()).toInstant();

            String result = cornExpressionBuilder.buildCornExpression(MedicineFrequency.THIRD_TIMES,
                    ReminderFrequency.DAILY, null, customStartDate);

            // 20, 20+8=28%24=4, 20+16=36%24=12
            assertEquals("0 0 20,4,12 * * ?", result);
        }
    }
}
