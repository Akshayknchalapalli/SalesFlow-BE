package com.salesflow.contact.core.mapper;

import com.salesflow.contact.core.domain.SocialProfile;
import com.salesflow.contact.core.dto.SocialProfileDTO;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-05-10T18:13:04+0530",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.12 (Oracle Corporation)"
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
