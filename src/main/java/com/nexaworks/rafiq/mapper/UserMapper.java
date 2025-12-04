package com.nexaworks.rafiq.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.nexaworks.rafiq.dto.request.user.UserRegistrationRequest;
import com.nexaworks.rafiq.entities.Doctor;
import com.nexaworks.rafiq.entities.Patient;
import com.nexaworks.rafiq.entities.enums.Gender;

import jakarta.validation.Valid;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "gender", expression = "java(parseGender(request.gender()))")
    Patient toUser(UserRegistrationRequest request);

    default Gender parseGender(String gender) {
        if (gender == null)
            return null;
        String g = gender.trim().toLowerCase();
        if ("male".equals(g))
            return Gender.MALE;
        if ("female".equals(g))
            return Gender.FEMALE;
        return null;
    }

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "gender", expression = "java(parseGender(user.gender()))")
    Doctor toDoctor(@Valid UserRegistrationRequest user);
}
