package com.nexaworks.rafiq.user.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nexaworks.rafiq.user.entity.model.Role;

public interface RoleRepository extends JpaRepository<Role, UUID> {
    Role findByName(String string);

}
