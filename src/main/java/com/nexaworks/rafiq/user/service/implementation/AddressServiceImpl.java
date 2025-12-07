package com.nexaworks.rafiq.user.service.implementation;

import java.util.List;

import org.springframework.stereotype.Service;

import com.nexaworks.rafiq.shared.repository.AddressRepository;
import com.nexaworks.rafiq.user.entity.model.Address;
import com.nexaworks.rafiq.user.service.AddressService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AddressServiceImpl implements AddressService {
    private final AddressRepository addressRepository;

    @Override
    public List<Address> saveAll(List<Address> entity) {
        return addressRepository.saveAll(entity);
    }

    @Override
    public List<Address> updateAddresses(List<Address> entity) {
        return entity.stream().map(this::updateAddress).toList();
    }

    @Override
    public Address updateAddress(Address entity) {
        return null;
    }

    @Override
    @Transactional
    public void deleteAll(List<Address> addresses) {
        addressRepository.deleteAll(addresses);
    }
}
