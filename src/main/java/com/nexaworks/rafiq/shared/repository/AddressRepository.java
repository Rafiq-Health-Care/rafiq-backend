package com.nexaworks.rafiq.shared.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nexaworks.rafiq.shared.entity.Address;

public interface AddressRepository extends JpaRepository<Address, UUID> {
}
