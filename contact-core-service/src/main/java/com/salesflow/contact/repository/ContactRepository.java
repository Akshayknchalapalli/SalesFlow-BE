package com.salesflow.contact.repository;

import com.salesflow.contact.domain.Contact;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ContactRepository extends JpaRepository<Contact, UUID> {
    
    @Query("SELECT c FROM Contact c WHERE c.email = :email")
    Optional<Contact> findByEmail(@Param("email") String email);
    
    @Query("SELECT c FROM Contact c WHERE c.ownerId = :ownerId")
    Page<Contact> findByOwnerId(@Param("ownerId") String ownerId, Pageable pageable);
    
    @Query("SELECT c FROM Contact c WHERE c.ownerId = :ownerId AND " +
           "LOWER(c.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(c.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(c.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(c.companyName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(c.phone) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Contact> searchContacts(@Param("ownerId") String ownerId,
                                @Param("searchTerm") String searchTerm,
                                Pageable pageable);
    
    @Query("SELECT c FROM Contact c WHERE c.ownerId = :ownerId AND c.stage = :stage")
    List<Contact> findByOwnerIdAndStage(@Param("ownerId") String ownerId,
                                       @Param("stage") Contact.ContactStage stage);
    
    @Query("SELECT COUNT(c) FROM Contact c WHERE c.ownerId = :ownerId AND c.stage = :stage")
    long countByOwnerIdAndStage(@Param("ownerId") String ownerId,
                               @Param("stage") Contact.ContactStage stage);
    
    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Contact c " +
           "WHERE c.email = :email AND c.id != :id")
    boolean existsByEmailAndIdNot(@Param("email") String email, @Param("id") UUID id);
    
    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Contact c " +
           "WHERE c.email = :email")
    boolean existsByEmail(@Param("email") String email);
} 