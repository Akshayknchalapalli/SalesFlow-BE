package com.salesflow.contact.mapper;

import com.salesflow.contact.domain.Address;
import com.salesflow.contact.dto.AddressDTO;
import org.mapstruct.Mapper;
 
@Mapper(componentModel = "spring")
public interface AddressMapper {
    Address toEntity(AddressDTO dto);
    AddressDTO toDTO(Address entity);
} 