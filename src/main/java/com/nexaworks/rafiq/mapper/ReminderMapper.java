package com.nexaworks.rafiq.mapper;

import java.util.Collections;

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
                .startDate(request.startDate()).endDate(request.endDate())
                .vibrate(request.vibrate()).build();
    }

    default AddReminderResponse toAddReminderResponse(Reminder savedReminder) {
        return new AddReminderResponse(savedReminder.getId(),
                savedReminder.getPatient().getUser().getId(), savedReminder.getMedicine().getId(),
                savedReminder.getMedicine().getName(), savedReminder.getMedicine().getDosage(),
                savedReminder.getStartDate().toString(),
                savedReminder.getMedicine().getReminderFrequency(),
                savedReminder.getMedicine().getCustomDays() != null
                        ? savedReminder.getMedicine().getCustomDays()
                        : Collections.emptyList(),
                savedReminder.getStartDate(), savedReminder.getEndDate(),
                savedReminder.getMedicine().getNotes(), savedReminder.isVibrate(),
                savedReminder.getMedicine().getStatus(), savedReminder.getCreatedAt(),
                savedReminder.getUpdatedAt());
    }
}
