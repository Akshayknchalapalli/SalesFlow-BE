package com.salesflow.contact.core.mapper;

import com.salesflow.contact.core.domain.Address;
import com.salesflow.contact.core.dto.AddressDTO;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-05-10T18:13:04+0530",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.12 (Oracle Corporation)"
)
@Component
public class AddressMapperImpl implements AddressMapper {

    @Override
    public Address toEntity(AddressDTO dto) {
        if ( dto == null ) {
            return null;
        }

        Address address = new Address();

        address.setType( dto.getType() );
        address.setStreet( dto.getStreet() );
        address.setCity( dto.getCity() );
        address.setState( dto.getState() );
        address.setPostalCode( dto.getPostalCode() );
        address.setCountry( dto.getCountry() );
        address.setPrimary( dto.isPrimary() );

        return address;
    }

    @Override
    public AddressDTO toDTO(Address entity) {
        if ( entity == null ) {
            return null;
        }

        AddressDTO addressDTO = new AddressDTO();

        addressDTO.setType( entity.getType() );
        addressDTO.setStreet( entity.getStreet() );
        addressDTO.setCity( entity.getCity() );
        addressDTO.setState( entity.getState() );
        addressDTO.setPostalCode( entity.getPostalCode() );
        addressDTO.setCountry( entity.getCountry() );
        addressDTO.setPrimary( entity.isPrimary() );

        return addressDTO;
    }
}
