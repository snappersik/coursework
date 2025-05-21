package com.almetpt.coursework.bookclub.repository;

import com.almetpt.coursework.bookclub.model.EventApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventApplicationRepository extends JpaRepository<EventApplication, Long> {

    @Query("SELECT COUNT(ea) FROM EventApplication ea WHERE ea.event.id = :eventId AND ea.status = 'APPROVED'")
    int countApprovedApplicationsForEvent(@Param("eventId") Long eventId);

    @Query("SELECT ea FROM EventApplication ea WHERE ea.event.id = :eventId AND ea.status = 'APPROVED'")
    List<EventApplication> findApprovedApplicationsForEvent(@Param("eventId") Long eventId);

    @Query("SELECT ea FROM EventApplication ea JOIN FETCH ea.user JOIN FETCH ea.event WHERE ea.user.id = :userId AND ea.isDeleted = false")
    List<EventApplication> findByUser_IdAndIsDeletedFalse(@Param("userId") Long userId);

    // Добавляем новый метод для проверки существующих заявок
    List<EventApplication> findByUser_IdAndEvent_IdAndIsDeletedFalse(Long userId, Long eventId);

    @Query("SELECT ea FROM EventApplication ea WHERE ea.event.id = :eventId AND ea.status = 'APPROVED' AND ea.attended = false")
    List<EventApplication> findApprovedNotAttendedApplicationsForEvent(@Param("eventId") Long eventId);
}
