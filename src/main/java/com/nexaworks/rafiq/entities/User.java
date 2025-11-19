package com.nexaworks.rafiq.entities;

import java.security.Principal;
import java.util.*;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.nexaworks.rafiq.enums.Gender;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Entity
@Table(name = "users")
public class User extends BaseEntity implements UserDetails, Principal {

    @Email
    @NotBlank
    @Column(unique = true, nullable = false)
    private String email;

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    @JsonIgnore
    private String password;
    @NotBlank
    private String firstName;
    private String lastName;
    private String phone;
    @Min(0)
    @Max(120)
    private int age;
    @Builder.Default
    private boolean active = true;
    @Builder.Default
    private boolean locked = false;
    @Builder.Default
    private boolean enabled = false;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "role_id"))
    @Builder.Default
    private Set<Role> roles = new HashSet<>();

    @OneToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE,
            CascadeType.REMOVE}, orphanRemoval = true)
    @JoinColumn(name = "doctor_profile_id", referencedColumnName = "id")
    private DoctorProfile doctorProfile;

    @OneToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE,
            CascadeType.REMOVE}, orphanRemoval = true)
    @JoinColumn(name = "patient_profile_id", referencedColumnName = "id")
    private PatientProfile patientProfile;

    @OneToMany(mappedBy = "user", cascade = {CascadeType.PERSIST, CascadeType.MERGE,
            CascadeType.REMOVE}, orphanRemoval = true)
    @Builder.Default
    private List<Address> addresses = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = {CascadeType.PERSIST, CascadeType.MERGE,
            CascadeType.REMOVE}, orphanRemoval = true)
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

    public boolean isDoctor() {
        return this.doctorProfile != null;
    }
    public boolean isPatient() {
        return this.patientProfile != null;
    }
    public void addRole(Role role) {
        this.roles.add(role);
    }
    public void removeRole(Role role) {
        this.roles.remove(role);
    }

}
