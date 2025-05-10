package com.salesflow.contact.core.mapper;

import com.salesflow.contact.core.domain.ContactPreferences;
import com.salesflow.contact.core.dto.ContactPreferencesDTO;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-05-10T18:13:04+0530",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.12 (Oracle Corporation)"
)
@Component
public class ContactPreferencesMapperImpl implements ContactPreferencesMapper {

    @Override
    public ContactPreferences toEntity(ContactPreferencesDTO dto) {
        if ( dto == null ) {
            return null;
        }

        ContactPreferences contactPreferences = new ContactPreferences();

        contactPreferences.setPreferredContactMethod( dto.getPreferredContactMethod() );
        contactPreferences.setPreferredContactTime( dto.getPreferredContactTime() );
        contactPreferences.setDoNotContact( dto.isDoNotContact() );
        contactPreferences.setMarketingOptIn( dto.isMarketingOptIn() );
        contactPreferences.setCommunicationLanguage( dto.getCommunicationLanguage() );

        return contactPreferences;
    }

    @Override
    public ContactPreferencesDTO toDTO(ContactPreferences entity) {
        if ( entity == null ) {
            return null;
        }

        ContactPreferencesDTO contactPreferencesDTO = new ContactPreferencesDTO();

        contactPreferencesDTO.setPreferredContactMethod( entity.getPreferredContactMethod() );
        contactPreferencesDTO.setPreferredContactTime( entity.getPreferredContactTime() );
        contactPreferencesDTO.setDoNotContact( entity.isDoNotContact() );
        contactPreferencesDTO.setMarketingOptIn( entity.isMarketingOptIn() );
        contactPreferencesDTO.setCommunicationLanguage( entity.getCommunicationLanguage() );

        return contactPreferencesDTO;
    }
}
