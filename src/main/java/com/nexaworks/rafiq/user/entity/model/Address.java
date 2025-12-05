package com.nexaworks.rafiq.user.entity.model;

import com.nexaworks.rafiq.entities.BaseEntity;
import com.nexaworks.rafiq.entities.Lab;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

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

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "lab_id")
    private Lab lab;
}
