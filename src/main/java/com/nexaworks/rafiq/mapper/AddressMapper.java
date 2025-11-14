package com.nexaworks.rafiq.mapper;

import java.util.List;

import org.springframework.stereotype.Component;

import com.nexaworks.rafiq.dto.request.AddAddressRequest;
import com.nexaworks.rafiq.entities.Address;

@Component
public class AddressMapper {

    public Address toEntity(AddAddressRequest request) {
        if (request == null)
            return null;
        return Address.builder().street(request.street()).city(request.city())
                .state(request.state()).country(request.country()).postalCode(request.postalCode())
                .build();
    }

    public List<Address> toEntity(List<AddAddressRequest> requests) {
        return requests.stream().map(this::toEntity).toList();
    }
}
