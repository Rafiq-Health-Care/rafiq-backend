package com.nexaworks.rafiq.mapper;

import java.time.Instant;
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
    default Reminder toEntity(AddReminderRequest addReminderRequest,
            MedicineService medicineService) {
        if (addReminderRequest == null) {
            return null;
        }
        return Reminder.builder()
                .medicine(medicineService.getMedicineById(addReminderRequest.medicineId()))
                .hour(Integer.parseInt(addReminderRequest.time().split(":")[0]))
                .minute(Integer.parseInt(addReminderRequest.time().split(":")[1]))
                .frequency(addReminderRequest.frequency())
                .startDate(addReminderRequest.startDate() == null
                        ? Instant.now()
                        : addReminderRequest.startDate())
                .endDate(addReminderRequest.endDate()).vibrate(addReminderRequest.vibrate())
                .customDays(addReminderRequest.dayOfWeek() == null
                        ? Collections.emptyList()
                        : addReminderRequest.dayOfWeek())
                .build();
    }

    default AddReminderResponse toAddReminderResponse(Reminder savedReminder) {
        if (savedReminder == null) {
            return null;
        }
        String time = String.format("%02d:%02d", savedReminder.getHour(),
                savedReminder.getMinute());
        return new AddReminderResponse(savedReminder.getId(), savedReminder.getPatient().getId(),
                savedReminder.getMedicine().getId(), savedReminder.getMedicine().getName(),
                savedReminder.getMedicine().getDosage(), time, savedReminder.getFrequency(),
                savedReminder.getCustomDays(), savedReminder.getStartDate(),
                savedReminder.getEndDate(), savedReminder.getMedicine().getNotes(),
                savedReminder.isVibrate(), savedReminder.getMedicine().getStatus(),
                savedReminder.getCreatedAt(), savedReminder.getUpdatedAt()

        );

    }
}
