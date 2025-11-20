package com.nexaworks.rafiq.mapper;

import org.mapstruct.Mapper;

import com.nexaworks.rafiq.dto.response.ReminderResponse;
import com.nexaworks.rafiq.entities.Reminder;

@Mapper(componentModel = "spring")
public interface ReminderMapper {

    ReminderResponse toResponse(Reminder reminder);
}
