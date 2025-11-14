package com.nexaworks.rafiq.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nexaworks.rafiq.entities.Role;

public interface RoleRepository extends JpaRepository<Role, UUID> {
    Role findByName(String string);

    boolean existsByName(String name);
}
