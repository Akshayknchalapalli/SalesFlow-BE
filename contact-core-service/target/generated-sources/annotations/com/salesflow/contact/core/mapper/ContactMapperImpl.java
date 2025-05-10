package com.salesflow.contact.core.mapper;

import com.salesflow.contact.core.domain.Address;
import com.salesflow.contact.core.domain.Contact;
import com.salesflow.contact.core.domain.SocialProfile;
import com.salesflow.contact.core.dto.AddressDTO;
import com.salesflow.contact.core.dto.ContactDTO;
import com.salesflow.contact.core.dto.SocialProfileDTO;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.annotation.processing.Generated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-05-10T13:02:54+0530",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.42.0.z20250331-1358, environment: Java 21.0.6 (Eclipse Adoptium)"
)
@Component
public class ContactMapperImpl implements ContactMapper {

    @Autowired
    private ContactPreferencesMapper contactPreferencesMapper;
    @Autowired
    private AddressMapper addressMapper;
    @Autowired
    private SocialProfileMapper socialProfileMapper;

    @Override
    public Contact toEntity(ContactDTO dto) {
        if ( dto == null ) {
            return null;
        }

        Contact.ContactBuilder<?, ?> contact = Contact.builder();

        contact.addresses( addressDTOSetToAddressSet( dto.getAddresses() ) );
        contact.companyName( dto.getCompanyName() );
        contact.email( dto.getEmail() );
        contact.firstName( dto.getFirstName() );
        contact.jobTitle( dto.getJobTitle() );
        contact.lastName( dto.getLastName() );
        contact.notes( dto.getNotes() );
        contact.ownerId( dto.getOwnerId() );
        contact.phone( dto.getPhone() );
        contact.preferences( contactPreferencesMapper.toEntity( dto.getPreferences() ) );
        contact.socialProfiles( socialProfileDTOSetToSocialProfileSet( dto.getSocialProfiles() ) );
        contact.stage( dto.getStage() );

        return contact.build();
    }

    @Override
    public ContactDTO toDTO(Contact entity) {
        if ( entity == null ) {
            return null;
        }

        ContactDTO contactDTO = new ContactDTO();

        contactDTO.setAddresses( addressSetToAddressDTOSet( entity.getAddresses() ) );
        contactDTO.setCompanyName( entity.getCompanyName() );
        contactDTO.setCreatedAt( entity.getCreatedAt() );
        contactDTO.setCreatedBy( entity.getCreatedBy() );
        contactDTO.setEmail( entity.getEmail() );
        contactDTO.setFirstName( entity.getFirstName() );
        contactDTO.setId( entity.getId() );
        contactDTO.setJobTitle( entity.getJobTitle() );
        contactDTO.setLastName( entity.getLastName() );
        contactDTO.setNotes( entity.getNotes() );
        contactDTO.setOwnerId( entity.getOwnerId() );
        contactDTO.setPhone( entity.getPhone() );
        contactDTO.setPreferences( contactPreferencesMapper.toDTO( entity.getPreferences() ) );
        contactDTO.setSocialProfiles( socialProfileSetToSocialProfileDTOSet( entity.getSocialProfiles() ) );
        contactDTO.setStage( entity.getStage() );
        contactDTO.setUpdatedAt( entity.getUpdatedAt() );
        contactDTO.setUpdatedBy( entity.getUpdatedBy() );
        contactDTO.setVersion( entity.getVersion() );

        return contactDTO;
    }

    @Override
    public void updateEntityFromDTO(ContactDTO dto, Contact entity) {
        if ( dto == null ) {
            return;
        }

        if ( entity.getAddresses() != null ) {
            Set<Address> set = addressDTOSetToAddressSet( dto.getAddresses() );
            if ( set != null ) {
                entity.getAddresses().clear();
                entity.getAddresses().addAll( set );
            }
        }
        else {
            Set<Address> set = addressDTOSetToAddressSet( dto.getAddresses() );
            if ( set != null ) {
                entity.setAddresses( set );
            }
        }
        if ( dto.getCompanyName() != null ) {
            entity.setCompanyName( dto.getCompanyName() );
        }
        if ( dto.getEmail() != null ) {
            entity.setEmail( dto.getEmail() );
        }
        if ( dto.getFirstName() != null ) {
            entity.setFirstName( dto.getFirstName() );
        }
        if ( dto.getJobTitle() != null ) {
            entity.setJobTitle( dto.getJobTitle() );
        }
        if ( dto.getLastName() != null ) {
            entity.setLastName( dto.getLastName() );
        }
        if ( dto.getNotes() != null ) {
            entity.setNotes( dto.getNotes() );
        }
        if ( dto.getOwnerId() != null ) {
            entity.setOwnerId( dto.getOwnerId() );
        }
        if ( dto.getPhone() != null ) {
            entity.setPhone( dto.getPhone() );
        }
        if ( dto.getPreferences() != null ) {
            entity.setPreferences( contactPreferencesMapper.toEntity( dto.getPreferences() ) );
        }
        if ( entity.getSocialProfiles() != null ) {
            Set<SocialProfile> set1 = socialProfileDTOSetToSocialProfileSet( dto.getSocialProfiles() );
            if ( set1 != null ) {
                entity.getSocialProfiles().clear();
                entity.getSocialProfiles().addAll( set1 );
            }
        }
        else {
            Set<SocialProfile> set1 = socialProfileDTOSetToSocialProfileSet( dto.getSocialProfiles() );
            if ( set1 != null ) {
                entity.setSocialProfiles( set1 );
            }
        }
        if ( dto.getStage() != null ) {
            entity.setStage( dto.getStage() );
        }
    }

    protected Set<Address> addressDTOSetToAddressSet(Set<AddressDTO> set) {
        if ( set == null ) {
            return null;
        }

        Set<Address> set1 = new LinkedHashSet<Address>( Math.max( (int) ( set.size() / .75f ) + 1, 16 ) );
        for ( AddressDTO addressDTO : set ) {
            set1.add( addressMapper.toEntity( addressDTO ) );
        }

        return set1;
    }

    protected Set<SocialProfile> socialProfileDTOSetToSocialProfileSet(Set<SocialProfileDTO> set) {
        if ( set == null ) {
            return null;
        }

        Set<SocialProfile> set1 = new LinkedHashSet<SocialProfile>( Math.max( (int) ( set.size() / .75f ) + 1, 16 ) );
        for ( SocialProfileDTO socialProfileDTO : set ) {
            set1.add( socialProfileMapper.toEntity( socialProfileDTO ) );
        }

        return set1;
    }

    protected Set<AddressDTO> addressSetToAddressDTOSet(Set<Address> set) {
        if ( set == null ) {
            return null;
        }

        Set<AddressDTO> set1 = new LinkedHashSet<AddressDTO>( Math.max( (int) ( set.size() / .75f ) + 1, 16 ) );
        for ( Address address : set ) {
            set1.add( addressMapper.toDTO( address ) );
        }

        return set1;
    }

    protected Set<SocialProfileDTO> socialProfileSetToSocialProfileDTOSet(Set<SocialProfile> set) {
        if ( set == null ) {
            return null;
        }

        Set<SocialProfileDTO> set1 = new LinkedHashSet<SocialProfileDTO>( Math.max( (int) ( set.size() / .75f ) + 1, 16 ) );
        for ( SocialProfile socialProfile : set ) {
            set1.add( socialProfileMapper.toDTO( socialProfile ) );
        }

        return set1;
    }
}
