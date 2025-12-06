package com.nexaworks.rafiq.shared.mapper;

import java.util.List;

import org.mapstruct.Mapper;

import com.nexaworks.rafiq.user.api.dto.request.AddAddressRequest;
import com.nexaworks.rafiq.shared.entity.Address;

@Mapper(componentModel = "spring")
public interface AddressMapper {

    Address toEntity(AddAddressRequest newAddress);
    List<Address> toEntity(List<AddAddressRequest> newAddresses);
}
