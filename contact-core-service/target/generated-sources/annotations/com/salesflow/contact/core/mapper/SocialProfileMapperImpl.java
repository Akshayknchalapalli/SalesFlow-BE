package com.salesflow.contact.core.mapper;

import com.salesflow.contact.core.domain.SocialProfile;
import com.salesflow.contact.core.dto.SocialProfileDTO;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-05-09T15:14:32+0530",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.42.0.z20250331-1358, environment: Java 21.0.6 (Eclipse Adoptium)"
)
@Component
public class SocialProfileMapperImpl implements SocialProfileMapper {

    @Override
    public SocialProfile toEntity(SocialProfileDTO dto) {
        if ( dto == null ) {
            return null;
        }

        SocialProfile socialProfile = new SocialProfile();

        socialProfile.setPlatform( dto.getPlatform() );
        socialProfile.setProfileUrl( dto.getProfileUrl() );
        socialProfile.setUsername( dto.getUsername() );
        socialProfile.setVerified( dto.isVerified() );

        return socialProfile;
    }

    @Override
    public SocialProfileDTO toDTO(SocialProfile entity) {
        if ( entity == null ) {
            return null;
        }

        SocialProfileDTO socialProfileDTO = new SocialProfileDTO();

        socialProfileDTO.setPlatform( entity.getPlatform() );
        socialProfileDTO.setProfileUrl( entity.getProfileUrl() );
        socialProfileDTO.setUsername( entity.getUsername() );
        socialProfileDTO.setVerified( entity.isVerified() );

        return socialProfileDTO;
    }
}
