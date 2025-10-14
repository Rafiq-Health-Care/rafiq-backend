package com.nexaworks.rafiq.repository;

import com.nexaworks.rafiq.entities.Address;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AddressRepository extends JpaRepository<Address, UUID> {
}
