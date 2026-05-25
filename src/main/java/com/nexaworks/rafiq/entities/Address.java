package com.nexaworks.rafiq.entities;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Entity
@EqualsAndHashCode(callSuper = false, of = {"street", "city", "state", "country", "postalCode",
        "latitude", "longitude"})
@ToString(exclude = {"user", "lab"})
@Table(name = "address", indexes = {@Index(name = "user_idx", columnList = "user_id")})
public class Address extends BaseEntity {
    private String street;
    private String city;
    private String state;
    private String country;
    private String postalCode;
    private double latitude;
    private double longitude;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Deprecated
    @ManyToOne
    @JoinColumn(name = "lab_id")
    private Lab lab;
}
