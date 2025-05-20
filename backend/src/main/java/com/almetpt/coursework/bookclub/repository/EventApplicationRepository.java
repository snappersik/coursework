package com.almetpt.coursework.bookclub.repository;

import com.almetpt.coursework.bookclub.model.EventApplication;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventApplicationRepository extends GenericRepository<EventApplication> {

    @Query("SELECT COUNT(ea) FROM EventApplication ea WHERE ea.event.id = :eventId AND ea.status = 'APPROVED'")
    int countApprovedApplicationsForEvent(@Param("eventId") Long eventId);

    @Query("SELECT ea FROM EventApplication ea WHERE ea.event.id = :eventId AND ea.status = 'APPROVED'")
    List<EventApplication> findApprovedApplicationsForEvent(@Param("eventId") Long eventId);

    List<EventApplication> findByUser_IdAndIsDeletedFalse(Long userId);

    @Query("SELECT ea FROM EventApplication ea WHERE ea.event.id = :eventId AND ea.status = 'APPROVED' AND ea.attended = false")
    List<EventApplication> findApprovedNotAttendedApplicationsForEvent(@Param("eventId") Long eventId);
    
}