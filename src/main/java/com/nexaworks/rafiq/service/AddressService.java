package com.nexaworks.rafiq.service;

import com.nexaworks.rafiq.entities.Address;
import java.util.List;

public interface AddressService {
  List<Address> saveAll(List<Address> entity);

  List<Address> updateAddresses(List<Address> entity);

  Address updateAddress(Address entity);

  void deleteAll(List<Address> addresses);
}
