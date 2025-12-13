package com.nexaworks.rafiq.user.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.nexaworks.rafiq.user.entity.model.User;

public interface UserRepository extends JpaRepository<User, UUID> {
    @EntityGraph(attributePaths = "roles")
    Optional<User> findByEmail(String email);

    boolean existsUserByEmail(String email);
}
