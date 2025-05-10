package com.salesflow.contact.core.mapper;

import com.salesflow.contact.core.domain.Address;
import com.salesflow.contact.core.dto.AddressDTO;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-05-10T13:02:54+0530",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.42.0.z20250331-1358, environment: Java 21.0.6 (Eclipse Adoptium)"
)
@Component
public class AddressMapperImpl implements AddressMapper {

    @Override
    public Address toEntity(AddressDTO dto) {
        if ( dto == null ) {
            return null;
        }

        Address address = new Address();

        address.setCity( dto.getCity() );
        address.setCountry( dto.getCountry() );
        address.setPostalCode( dto.getPostalCode() );
        address.setPrimary( dto.isPrimary() );
        address.setState( dto.getState() );
        address.setStreet( dto.getStreet() );
        address.setType( dto.getType() );

        return address;
    }

    @Override
    public AddressDTO toDTO(Address entity) {
        if ( entity == null ) {
            return null;
        }

        AddressDTO addressDTO = new AddressDTO();

        addressDTO.setCity( entity.getCity() );
        addressDTO.setCountry( entity.getCountry() );
        addressDTO.setPostalCode( entity.getPostalCode() );
        addressDTO.setPrimary( entity.isPrimary() );
        addressDTO.setState( entity.getState() );
        addressDTO.setStreet( entity.getStreet() );
        addressDTO.setType( entity.getType() );

        return addressDTO;
    }
}
