package com.nexaworks.rafiq.repository;

import com.nexaworks.rafiq.entities.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RoleRepository extends JpaRepository<Role, UUID> {
    Role findByName(String string);

    boolean existsByName(String name);
}
