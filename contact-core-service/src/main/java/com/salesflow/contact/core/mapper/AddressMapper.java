package com.salesflow.contact.core.mapper;

import com.salesflow.contact.core.domain.Address;
import com.salesflow.contact.core.dto.AddressDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AddressMapper {
    Address toEntity(AddressDTO dto);
    AddressDTO toDTO(Address entity);
} 