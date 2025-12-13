package com.nexaworks.rafiq.medication.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.nexaworks.rafiq.medication.api.dto.response.DrugSearchResponse;
import com.nexaworks.rafiq.medication.entity.model.Drug;

@Mapper(componentModel = "spring")
public interface DrugMapper {

    @Mapping(source = "tradeName", target = "name")
    @Mapping(source = "id", target = "drugId")
    DrugSearchResponse toDto(Drug drug);
}
