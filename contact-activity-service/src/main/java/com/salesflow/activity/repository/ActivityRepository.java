package com.salesflow.activity.repository;

import com.salesflow.activity.model.Activity;
import com.salesflow.activity.model.ActivityType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface ActivityRepository extends JpaRepository<Activity, UUID> {
    List<Activity> findByContactId(UUID contactId);
    
    List<Activity> findByContactIdAndType(UUID contactId, ActivityType type);
    
    List<Activity> findByAssignedToAndCompletedTimeIsNull(String assignedTo);
    
    List<Activity> findByScheduledTimeBetween(LocalDateTime startTime, LocalDateTime endTime);
    
    @Query("SELECT a FROM Activity a WHERE a.contactId = :contactId AND a.scheduledTime BETWEEN :startDate AND :endDate")
    List<Activity> findByContactIdAndDateRange(
        @Param("contactId") UUID contactId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    @Query("SELECT a FROM Activity a WHERE " +
           "(:query IS NULL OR LOWER(a.title) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(a.description) LIKE LOWER(CONCAT('%', :query, '%'))) AND " +
           "(:type IS NULL OR a.type = :type) AND " +
           "(:assignedTo IS NULL OR a.assignedTo = :assignedTo) AND " +
           "(:startDate IS NULL OR a.scheduledTime >= :startDate) AND " +
           "(:endDate IS NULL OR a.scheduledTime <= :endDate)")
    List<Activity> searchActivities(
        @Param("query") String query,
        @Param("type") ActivityType type,
        @Param("assignedTo") String assignedTo,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
} 