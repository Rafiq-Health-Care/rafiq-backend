package com.nexaworks.rafiq.user.mapper;

import java.util.List;

import org.mapstruct.Mapper;

import com.nexaworks.rafiq.user.api.dto.request.AddAddressRequest;
import com.nexaworks.rafiq.user.entity.model.Address;

@Mapper(componentModel = "spring")
public interface AddressMapper {

    Address toEntity(AddAddressRequest newAddress);
    List<Address> toEntity(List<AddAddressRequest> newAddresses);
}
