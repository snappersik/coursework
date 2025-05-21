package com.almetpt.coursework.bookclub.repository;

import com.almetpt.coursework.bookclub.model.EventApplication;
// Убедитесь, что GenericRepository импортирован, если он в другом пакете, или удалите этот импорт, если он не нужен явно
// import com.almetpt.coursework.bookclub.repository.GenericRepository; // Этот импорт может быть излишен, если он в том же пакете или уже доступен
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
// ИЗМЕНЕНИЕ: Заменяем JpaRepository на GenericRepository
public interface EventApplicationRepository extends GenericRepository<EventApplication> {

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
