package com.nexaworks.rafiq.mapper;

import com.nexaworks.rafiq.dto.request.UserRegistrationRequest;
import com.nexaworks.rafiq.entities.User;
import com.nexaworks.rafiq.enums.Gender;
import jakarta.validation.Valid;

public class UserMapper {

    public static User toUser(@Valid UserRegistrationRequest request) {
        if (request == null) {
            return null;
        }
        return User.builder()
                .email(request.email())
                // Save password as plain text as requested (no encoding here)
                .password(request.password())
                .firstName(request.firstName())
                .lastName(request.lastName())
                .phone(request.phone())
                .age(request.age())
                .gender(parseGender(request.gender()))
                .active(true)
                .locked(false)
                .enabled(false)
                .build();
    }

    private static Gender parseGender(String gender) {
        if (gender == null) return null;
        String g = gender.trim().toLowerCase();
        if ("male".equals(g)) return Gender.MALE;
        if ("female".equals(g)) return Gender.FEMALE;
        // Fallback: return null if not recognized; validation should prevent this
        return null;
    }
}
