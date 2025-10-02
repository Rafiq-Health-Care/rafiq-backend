package com.nexaworks.rafiq.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
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
    @Id
    public UUID id;
    private String street;
    private String city;
    private String state;
    private String country;
    private String postalCode;

}
