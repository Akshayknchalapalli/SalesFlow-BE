package com.salesflow.contact.mapper;

import com.salesflow.contact.domain.Contact;
import com.salesflow.contact.dto.ContactDTO;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring",
        uses = {ContactPreferencesMapper.class, AddressMapper.class, SocialProfileMapper.class},
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ContactMapper {
    Contact toEntity(ContactDTO dto);
    ContactDTO toDTO(Contact entity);
    void updateEntityFromDTO(ContactDTO dto, @MappingTarget Contact entity);
} 