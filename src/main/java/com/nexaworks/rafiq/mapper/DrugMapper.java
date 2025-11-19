package com.nexaworks.rafiq.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.nexaworks.rafiq.dto.response.DrugSearchResponse;
import com.nexaworks.rafiq.entities.Drug;

@Mapper(componentModel = "spring")
public interface DrugMapper {

    @Mapping(source = "tradeName", target = "name")
    @Mapping(source = "id", target = "drugId")
    DrugSearchResponse toDto(Drug drug);
}
