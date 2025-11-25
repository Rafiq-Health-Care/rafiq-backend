package com.nexaworks.rafiq.secheduler.service.serviceImpl;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import org.springframework.stereotype.Service;

import com.nexaworks.rafiq.entities.enums.Day;
import com.nexaworks.rafiq.entities.enums.MedicineFrequency;
import com.nexaworks.rafiq.entities.enums.ReminderFrequency;
import com.nexaworks.rafiq.secheduler.service.CornExpressionBuilder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class CornExpressionBuilderImpl implements CornExpressionBuilder {
    public String buildCornExpression(MedicineFrequency medicineFrequency,
            ReminderFrequency reminderFrequency, List<Day> customDay, Instant startDate) {
        LocalDateTime startTake = LocalDateTime.ofInstant(startDate, ZoneId.systemDefault());

        int startHour = startTake.getHour();
        int startMinute = startTake.getMinute();
        int startDay = startTake.getDayOfWeek().getValue();

        return switch (reminderFrequency) {
            case DAILY -> buildDailyCornExpression(startHour, startMinute, medicineFrequency);
            case WEEKLY ->
                buildWeeklyCornExpression(startHour, startMinute, startDay, medicineFrequency);
            case MONTHLY ->
                buildMonthlyCornExpression(startHour, startMinute, startDay, medicineFrequency);
            case YEARLY ->
                buildYearlyCornExpression(startHour, startMinute, startDay, medicineFrequency);
            case CUSTOM ->
                buildCustomCornExpression(startHour, startMinute, customDay, medicineFrequency);
        };
    }

    private String buildCustomCornExpression(int startHour, int startMinute, List<Day> customDay,
            MedicineFrequency medicineFrequency) {
        StringBuilder cornExpression = new StringBuilder();
        for (int i = 0; i < customDay.size(); i++) {
            Day day = customDay.get(i);
            int dayOfWeek = day.ordinal() + 1; // MONDAY=1, SUNDAY=7
            if (i > 0) {
                cornExpression.append(",");
            }
            cornExpression.append(dayOfWeek);
        }
        return String.format("0 %d %d ? * %s", startMinute, startHour, cornExpression);
    }

    private String buildYearlyCornExpression(int startHour, int startMinute, int startDay,
            MedicineFrequency medicineFrequency) {
        int numberOfMonths = 12 / medicineFrequency.getNumberOfTimes();
        StringBuilder cornExpression = new StringBuilder();
        for (int i = 0; i < medicineFrequency.getNumberOfTimes(); i++) {
            int month = ((i * numberOfMonths) % 12) + 1; // 1=January, 12=December
            if (i > 0) {
                cornExpression.append(",");
            }
            cornExpression.append(month);
        }
        return String.format("0 %d %d %d * ? %s", startMinute, startHour, startDay, cornExpression);
    }

    private String buildMonthlyCornExpression(int startHour, int startMinute, int startDay,
            MedicineFrequency medicineFrequency) {
        int numberOfDays = 30 / medicineFrequency.getNumberOfTimes();
        StringBuilder cornExpression = new StringBuilder();
        for (int i = 0; i < medicineFrequency.getNumberOfTimes(); i++) {
            int dayOfMonth = ((i * numberOfDays) % 30) + 1; // 1-30 for simplicity
            if (i > 0) {
                cornExpression.append(",");
            }
            cornExpression.append(dayOfMonth);
        }
        return String.format("0 %d %d %s * ?", startMinute, startHour, cornExpression);
    }

    private String buildWeeklyCornExpression(int startHour, int startMinute, int startDay,
            MedicineFrequency medicineFrequency) {
        int numberOfDays = 7 / medicineFrequency.getNumberOfTimes();
        StringBuilder cornExpression = new StringBuilder();
        for (int i = 0; i < medicineFrequency.getNumberOfTimes(); i++) {
            int dayOfWeek = ((startDay - 1 + (i * numberOfDays)) % 7) + 1; // 1=Monday, 7=Sunday
            if (i > 0) {
                cornExpression.append(",");
            }
            cornExpression.append(dayOfWeek);
        }
        return String.format("0 %d %d ? * %s", startMinute, startHour, cornExpression);
    }

    private String buildDailyCornExpression(int startHour, int startMinute,
            MedicineFrequency medicineFrequency) {
        int numberOfHours = 24 / medicineFrequency.getNumberOfTimes();
        StringBuilder cornExpression = new StringBuilder();
        for (int i = 0; i < medicineFrequency.getNumberOfTimes(); i++) {
            int hour = (startHour + (i * numberOfHours)) % 24;
            if (i > 0) {
                cornExpression.append(",");
            }
            cornExpression.append(hour);
        }
        return String.format("0 %d %s * * ?", startMinute, cornExpression);
    }
}
