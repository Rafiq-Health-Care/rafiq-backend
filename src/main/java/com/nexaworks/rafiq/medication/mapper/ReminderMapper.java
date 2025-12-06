package com.nexaworks.rafiq.medication.mapper;

import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.nexaworks.rafiq.medication.api.dto.request.AddReminderRequest;
import com.nexaworks.rafiq.medication.api.dto.response.AddReminderResponse;
import com.nexaworks.rafiq.medication.api.dto.response.GetReminderByIdResponse;
import com.nexaworks.rafiq.medication.api.dto.response.ReminderResponse;
import com.nexaworks.rafiq.medication.entity.model.Reminder;
import com.nexaworks.rafiq.medication.service.MedicineService;

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
