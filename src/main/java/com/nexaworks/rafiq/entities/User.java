package com.nexaworks.rafiq.entities;

import java.security.Principal;
import java.time.LocalDate;
import java.util.*;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.nexaworks.rafiq.entities.enums.Gender;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@ToString(exclude = {"password", "addresses", "tokens", "roles"})
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Entity
@Table(name = "users", indexes = {@Index(columnList = "email", unique = true)})
@Inheritance(strategy = InheritanceType.JOINED)
public class User extends BaseEntity implements UserDetails, Principal {

    @Email
    @NotBlank
    @Column(unique = true, nullable = false)
    private String email;

    @JsonIgnore
    private String password;

    @NotBlank
    private String firstName;
    private String lastName;
    private String phone;

    private LocalDate birthDate;

    @Builder.Default
    private boolean active = true;

    @Builder.Default
    private boolean locked = false;

    @Builder.Default
    private boolean enabled = false;

    private String notificationToken;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "role_id"))
    @Builder.Default
    private Set<Role> roles = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = {CascadeType.PERSIST, CascadeType.MERGE,
            CascadeType.REMOVE}, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Address> addresses = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = {CascadeType.PERSIST, CascadeType.MERGE,
            CascadeType.REMOVE}, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Token> tokens = new ArrayList<>();

    @Override
    public String getName() {
        return this.firstName + " " + this.lastName;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.roles.stream().map(role -> new SimpleGrantedAuthority(role.getName())).toList();
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return this.email;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !this.locked;
    }

    @Override
    public boolean isEnabled() {
        return this.enabled;
    }

    public void addRole(Role role) {
        this.roles.add(role);
    }
    public void removeRole(Role role) {
        this.roles.remove(role);
    }
}
