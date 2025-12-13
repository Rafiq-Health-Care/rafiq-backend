package com.nexaworks.rafiq.user.entity.model;

import com.nexaworks.rafiq.shared.entity.BaseEntity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Entity
@Table(name = "address", schema = "user_schema")
public class Address extends BaseEntity {
    private String street;
    private String city;
    private String state;
    private String country;
    private String postalCode;
    private Boolean isPrimary;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

}
