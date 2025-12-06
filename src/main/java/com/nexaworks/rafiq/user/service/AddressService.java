package com.nexaworks.rafiq.user.service;

import java.util.List;

import com.nexaworks.rafiq.shared.entity.Address;

public interface AddressService {
    List<Address> saveAll(List<Address> entity);

    List<Address> updateAddresses(List<Address> entity);

    Address updateAddress(Address entity);

    void deleteAll(List<Address> addresses);
}
