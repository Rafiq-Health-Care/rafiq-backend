package com.nexaworks.rafiq.mapper;

import java.util.List;

import org.mapstruct.Mapper;

import com.nexaworks.rafiq.dto.request.AddAddressRequest;
import com.nexaworks.rafiq.entities.Address;

@Mapper(componentModel = "spring")
public interface AddressMapper {

    Address toEntity(AddAddressRequest newAddress);
    List<Address> toEntity(List<AddAddressRequest> newAddresses);
}
