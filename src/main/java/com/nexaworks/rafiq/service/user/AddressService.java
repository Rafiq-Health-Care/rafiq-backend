package com.nexaworks.rafiq.service.user;

import java.util.List;

import com.nexaworks.rafiq.entities.Address;

public interface AddressService {
    List<Address> saveAll(List<Address> entity);

    List<Address> updateAddresses(List<Address> entity);

    Address updateAddress(Address entity);

    void deleteAll(List<Address> addresses);
}
