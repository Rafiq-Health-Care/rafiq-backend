package com.nexaworks.rafiq.dto.response.medicine;

import com.nexaworks.rafiq.dto.response.reminder.ReminderResponse;

import java.util.List;

public record GetMedicineResponse(MedicineResponse medicine, List<ReminderResponse> reminders) {
}
