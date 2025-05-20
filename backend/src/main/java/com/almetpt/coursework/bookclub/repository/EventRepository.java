package com.almetpt.coursework.bookclub.repository;

import com.almetpt.coursework.bookclub.model.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EventRepository extends GenericRepository<Event> {

    // Поиск мероприятий по названию, типу и диапазону дат
    @Query(nativeQuery = true, value = """
        SELECT *
        FROM events e
        WHERE (LOWER(e.title) LIKE LOWER(CONCAT('%', :title, '%')) OR :title IS NULL)
        AND (LOWER(e.event_type) LIKE LOWER(CONCAT('%', :eventType, '%')) OR :eventType IS NULL)
        AND (e.date >= :startDate OR :startDate IS NULL)
        AND (e.date <= :endDate OR :endDate IS NULL)
        AND e.is_deleted = false
        ORDER BY e.date DESC
        """)
    Page<Event> findEventsByParameters(
            @Param("title") String title,
            @Param("eventType") String eventType,
            @Param("startDate") @DateTimeFormat(pattern = "dd.MM.yyyy HH:mm") LocalDateTime startDate,
            @Param("endDate") @DateTimeFormat(pattern = "dd.MM.yyyy HH:mm") LocalDateTime endDate,
            Pageable pageable);

    // Подсчет количества одобренных заявок на мероприятие
    @Query(nativeQuery = true, value = """
        SELECT COUNT(*)
        FROM event_applications ea
        WHERE ea.event_id = :eventId
        AND ea.status = 'APPROVED'
        AND ea.is_deleted = false
        """)
    int countApprovedApplications(@Param("eventId") Long eventId);

    // Подсчет общего количества заявок на мероприятие
    @Query(nativeQuery = true, value = """
        SELECT COUNT(*)
        FROM event_applications ea
        WHERE ea.event_id = :eventId
        AND ea.is_deleted = false
        """)
    int countTotalApplications(@Param("eventId") Long eventId);

    // SOFT DELETE
    @Query("SELECT e FROM Event e WHERE e.date < :cutoffDate")
    List<Event> findEventsBeforeDate(
            @Param("cutoffDate") @DateTimeFormat(pattern = "dd.MM.yyyy HH:mm") LocalDateTime cutoffDate);

    @Query("""
        select case when count(e) > 0 then false else true end
        from Event e join EventApplication ea on e.id = ea.event.id
        where e.id = :id and ea.status = 'APPROVED' and e.date > CURRENT_TIMESTAMP
        """)
    boolean isEventCanBeDeleted(@Param("id") Long id);            
    
    long countByIsDeletedFalse();
    
    List<Event> findByIsDeletedFalseAndIsCancelledFalse();
}
