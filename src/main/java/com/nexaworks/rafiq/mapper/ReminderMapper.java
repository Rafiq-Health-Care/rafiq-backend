package com.nexaworks.rafiq.mapper;

import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.nexaworks.rafiq.dto.request.reminder.AddReminderRequest;
import com.nexaworks.rafiq.dto.response.reminder.AddReminderResponse;
import com.nexaworks.rafiq.dto.response.reminder.GetReminderByIdResponse;
import com.nexaworks.rafiq.dto.response.reminder.ReminderResponse;
import com.nexaworks.rafiq.entities.Reminder;
import com.nexaworks.rafiq.service.MedicineService;

@Mapper(componentModel = "spring", uses = MedicineService.class)
public interface ReminderMapper {

    ReminderResponse toResponse(Reminder reminder);

    @Mapping(target = "medicine", expression = "java(medicineService.getMedicineById(request.medicineId()))")
    Reminder toEntity(AddReminderRequest request, @Context MedicineService medicineService);

    @Mapping(target = "medicineId", source = "medicine.id")
    AddReminderResponse toAddReminderResponse(Reminder savedReminder);

    @Mapping(target = "dosage", expression = "java(reminderById.getMedicine().getDosage())")
    @Mapping(target = "medicineId", expression = "java(reminderById.getMedicine().getId())")
    @Mapping(target = "medicineName", expression = "java(reminderById.getMedicine().getName())")
    @Mapping(target = "notes", expression = "java(reminderById.getMedicine().getNotes())")
    @Mapping(target = "frequency", expression = "java(reminderById.getMedicine().getFrequency())")
    @Mapping(target = "reminderFrequency", expression = "java(reminderById.getMedicine().getReminderFrequency())")
    @Mapping(target = "customDays", expression = "java(reminderById.getMedicine().getCustomDays())")
    GetReminderByIdResponse toGetReminderByIdResponse(Reminder reminderById);

}
