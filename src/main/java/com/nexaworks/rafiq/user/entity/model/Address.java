package com.nexaworks.rafiq.user.entity.model;

import com.nexaworks.rafiq.shared.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Entity
public class Address extends BaseEntity {
    private String street;
    private String city;
    private String state;
    private String country;
    private String postalCode;
    private Boolean isPrimary;

    private UUID entityId;

}
