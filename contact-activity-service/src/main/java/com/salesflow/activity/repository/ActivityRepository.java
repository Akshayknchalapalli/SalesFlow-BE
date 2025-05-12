package com.salesflow.activity.repository;

import com.salesflow.activity.model.Activity;
import com.salesflow.activity.model.ActivityType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ActivityRepository extends JpaRepository<Activity, Long> {
    List<Activity> findByContactId(Long contactId);
    
    List<Activity> findByContactIdAndType(Long contactId, ActivityType type);
    
    List<Activity> findByAssignedTo(String assignedTo);
    
    List<Activity> findByScheduledAtBetween(LocalDateTime start, LocalDateTime end);
    
    List<Activity> findByContactIdAndCompletedAtIsNull(Long contactId);
    
    List<Activity> findByAssignedToAndCompletedAtIsNull(String assignedTo);
} 