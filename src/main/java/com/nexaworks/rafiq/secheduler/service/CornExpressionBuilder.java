package com.nexaworks.rafiq.secheduler.service;

import java.time.Instant;
import java.util.List;

import com.nexaworks.rafiq.entities.enums.Day;
import com.nexaworks.rafiq.entities.enums.MedicineFrequency;
import com.nexaworks.rafiq.entities.enums.ReminderFrequency;

public interface CornExpressionBuilder {
    String buildCornExpression(MedicineFrequency medicineFrequency,
            ReminderFrequency reminderFrequency, List<Day> customDay, Instant startDate);
}
