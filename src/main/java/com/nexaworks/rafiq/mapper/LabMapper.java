package com.nexaworks.rafiq.mapper;

import org.mapstruct.Mapper;

import com.nexaworks.rafiq.dto.response.lab.LabResponse;
import com.nexaworks.rafiq.entities.Lab;

@Mapper(componentModel = "spring")
@Deprecated
public interface LabMapper {

    LabResponse toDto(Lab lab);
}