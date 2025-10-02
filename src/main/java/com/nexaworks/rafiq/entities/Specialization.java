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
@Entity
@SuperBuilder
public class Specialization  extends BaseEntity{
    @Id
    private UUID id;
    private String name;
    private String description;
    private String code;

}
