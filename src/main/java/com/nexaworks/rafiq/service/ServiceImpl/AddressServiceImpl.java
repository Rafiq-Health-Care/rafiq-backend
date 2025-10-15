package com.nexaworks.rafiq.service.ServiceImpl;

import com.nexaworks.rafiq.entities.Address;
import com.nexaworks.rafiq.repository.AddressRepository;
import com.nexaworks.rafiq.service.AddressService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

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
