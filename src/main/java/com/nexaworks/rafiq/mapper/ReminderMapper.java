package com.nexaworks.rafiq.mapper;

import org.mapstruct.Mapper;

import com.nexaworks.rafiq.dto.request.reminder.AddReminderRequest;
import com.nexaworks.rafiq.dto.response.reminder.AddReminderResponse;
import com.nexaworks.rafiq.dto.response.reminder.ReminderResponse;
import com.nexaworks.rafiq.entities.Reminder;
import com.nexaworks.rafiq.service.MedicineService;

@Mapper(componentModel = "spring")
public interface ReminderMapper {

    ReminderResponse toResponse(Reminder reminder);

    default Reminder toEntity(AddReminderRequest request, MedicineService medicineService) {
        return Reminder.builder().medicine(medicineService.getMedicineById(request.medicineId()))
                .vibrate(request.vibrate()).build();
    }

    default AddReminderResponse toAddReminderResponse(Reminder savedReminder) {
        return new AddReminderResponse(savedReminder.getId(), savedReminder.getMedicine().getId(),
                savedReminder.isVibrate(), savedReminder.getCreatedAt(),
                savedReminder.getUpdatedAt());
    }
}
