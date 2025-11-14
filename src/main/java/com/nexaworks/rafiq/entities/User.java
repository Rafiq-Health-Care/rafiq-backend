package com.nexaworks.rafiq.entities;

import java.security.Principal;
import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.nexaworks.rafiq.enums.Gender;

import jakarta.persistence.*;
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

    @Column(unique = true)
    private String email;

    private String password;
    private String firstName;
    private String lastName;
    private String phone;
    private int age;
    private boolean active;
    private boolean locked;
    private boolean enabled;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @ManyToMany
    @JoinTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "role_id"))
    private List<Role> roles;

    @OneToOne(cascade = CascadeType.REMOVE)
    @JoinColumn(name = "doctor_profile_id", referencedColumnName = "id")
    private DoctorProfile doctorProfile;

    @OneToOne(cascade = CascadeType.REMOVE)
    @JoinColumn(name = "patient_profile_id", referencedColumnName = "id")
    private PatientProfile patientProfile;

    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE)
    private List<Address> addresses;

    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE)
    private List<Token> tokens;

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
}
