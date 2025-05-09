package com.salesflow.contact.core.repository;

import com.salesflow.contact.core.domain.Contact;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContactRepository extends JpaRepository<Contact, Long> {
    
    Optional<Contact> findByEmail(String email);
    
    Page<Contact> findByOwnerId(String ownerId, Pageable pageable);
    
    @Query("SELECT c FROM Contact c WHERE c.ownerId = :ownerId AND " +
           "LOWER(c.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(c.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(c.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(c.companyName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Contact> searchContacts(@Param("ownerId") String ownerId,
                                @Param("searchTerm") String searchTerm,
                                Pageable pageable);
    
    List<Contact> findByOwnerIdAndStage(String ownerId, Contact.ContactStage stage);
    
    @Query("SELECT COUNT(c) FROM Contact c WHERE c.ownerId = :ownerId AND c.stage = :stage")
    long countByOwnerIdAndStage(@Param("ownerId") String ownerId,
                               @Param("stage") Contact.ContactStage stage);
    
    boolean existsByEmailAndIdNot(String email, Long id);
} 