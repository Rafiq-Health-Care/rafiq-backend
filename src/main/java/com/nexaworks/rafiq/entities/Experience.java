package com.nexaworks.rafiq.entities;

import java.time.LocalDate;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class Experience {
    private UUID id;
    private String position;
    private String hospital;
    private LocalDate startDate;
    private LocalDate endDate;
    private String description;
    private boolean current;
}
