package com.salesflow.contact.core.mapper;

import com.salesflow.contact.core.domain.ContactPreferences;
import com.salesflow.contact.core.dto.ContactPreferencesDTO;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-05-11T00:34:16+0530",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.42.0.z20250331-1358, environment: Java 21.0.6 (Eclipse Adoptium)"
)
@Component
public class ContactPreferencesMapperImpl implements ContactPreferencesMapper {

    @Override
    public ContactPreferences toEntity(ContactPreferencesDTO dto) {
        if ( dto == null ) {
            return null;
        }

        ContactPreferences contactPreferences = new ContactPreferences();

        contactPreferences.setCommunicationLanguage( dto.getCommunicationLanguage() );
        contactPreferences.setDoNotContact( dto.isDoNotContact() );
        contactPreferences.setMarketingOptIn( dto.isMarketingOptIn() );
        contactPreferences.setPreferredContactMethod( dto.getPreferredContactMethod() );
        contactPreferences.setPreferredContactTime( dto.getPreferredContactTime() );

        return contactPreferences;
    }

    @Override
    public ContactPreferencesDTO toDTO(ContactPreferences entity) {
        if ( entity == null ) {
            return null;
        }

        ContactPreferencesDTO contactPreferencesDTO = new ContactPreferencesDTO();

        contactPreferencesDTO.setCommunicationLanguage( entity.getCommunicationLanguage() );
        contactPreferencesDTO.setDoNotContact( entity.isDoNotContact() );
        contactPreferencesDTO.setMarketingOptIn( entity.isMarketingOptIn() );
        contactPreferencesDTO.setPreferredContactMethod( entity.getPreferredContactMethod() );
        contactPreferencesDTO.setPreferredContactTime( entity.getPreferredContactTime() );

        return contactPreferencesDTO;
    }
}
