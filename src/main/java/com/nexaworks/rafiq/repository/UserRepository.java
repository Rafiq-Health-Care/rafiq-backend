package com.nexaworks.rafiq.repository;

import com.nexaworks.rafiq.entities.User;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, UUID> {
  @EntityGraph(attributePaths = "roles")
  Optional<User> findByEmail(String email);
}
