package com.salesflow.contact.mapper;

import com.salesflow.contact.domain.ContactPreferences;
import com.salesflow.contact.dto.ContactPreferencesDTO;
import com.salesflow.contact.domain.Contact.ContactMethod;
import com.salesflow.contact.domain.Contact.ContactTime;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.Named;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.InheritConfiguration;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ContactPreferencesMapper {
    
    @Mapping(target = "preferredContactMethod", source = "preferredContactMethod", qualifiedByName = "stringToContactMethod")
    @Mapping(target = "preferredContactTime", source = "preferredContactTime", qualifiedByName = "stringToContactTime")
    @Mapping(target = "doNotContact", source = "doNotContact")
    @Mapping(target = "marketingOptIn", source = "marketingOptIn")
    @Mapping(target = "communicationLanguage", source = "communicationLanguage")
    ContactPreferencesDTO toDTO(ContactPreferences entity);
    
    @InheritInverseConfiguration(name = "toDTO")
    @Mapping(target = "preferredContactMethod", source = "preferredContactMethod", qualifiedByName = "contactMethodToString")
    @Mapping(target = "preferredContactTime", source = "preferredContactTime", qualifiedByName = "contactTimeToString")
    ContactPreferences toEntity(ContactPreferencesDTO dto);

    @InheritConfiguration(name = "toEntity")
    void updateEntityFromDTO(ContactPreferencesDTO dto, @MappingTarget ContactPreferences entity);

    @Named("stringToContactMethod")
    default ContactMethod stringToContactMethod(String value) {
        return value == null ? null : ContactMethod.valueOf(value);
    }

    @Named("contactMethodToString")
    default String contactMethodToString(ContactMethod value) {
        return value == null ? null : value.name();
    }

    @Named("stringToContactTime")
    default ContactTime stringToContactTime(String value) {
        return value == null ? null : ContactTime.valueOf(value);
    }

    @Named("contactTimeToString")
    default String contactTimeToString(ContactTime value) {
        return value == null ? null : value.name();
    }
} 