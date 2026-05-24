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
@Table(name = "address")
@EqualsAndHashCode(callSuper = false, of = {"street", "city", "state", "country", "postalCode",
        "latitude", "longitude"})
@ToString(exclude = {"user", "lab"})
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
