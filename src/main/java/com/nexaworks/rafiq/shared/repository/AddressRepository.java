package com.nexaworks.rafiq.shared.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nexaworks.rafiq.user.entity.model.Address;

public interface AddressRepository extends JpaRepository<Address, UUID> {
}
