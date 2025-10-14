package com.nexaworks.rafiq.service;

import com.nexaworks.rafiq.entities.Address;

import java.util.List;

public interface AddressService {
    List<Address> saveAll(List<Address> entity);
}
