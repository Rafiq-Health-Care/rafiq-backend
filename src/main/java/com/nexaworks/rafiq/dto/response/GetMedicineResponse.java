package com.nexaworks.rafiq.dto.response;

import java.util.List;

public record GetMedicineResponse(MedicineResponse medicine, List<ReminderResponse> reminders) {
}
