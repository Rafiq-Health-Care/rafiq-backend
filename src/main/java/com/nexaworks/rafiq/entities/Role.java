package com.nexaworks.rafiq.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class Role {
    @Id
    @GeneratedValue
    private UUID id;
    private String name;
    @ManyToMany(mappedBy = "roles")
    private List<User> users;
}
