package com.nexaworks.rafiq.repository;

import com.nexaworks.rafiq.entities.Role;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, UUID> {
  Role findByName(String string);

  boolean existsByName(String name);
}
