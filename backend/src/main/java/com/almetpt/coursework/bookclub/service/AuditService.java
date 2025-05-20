package com.almetpt.coursework.bookclub.service;

import com.almetpt.coursework.bookclub.model.AuditEntry;
import com.almetpt.coursework.bookclub.repository.AuditRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

// import java.time.LocalDateTime;

@Service
public class AuditService {
    private static final Logger logger = LoggerFactory.getLogger(AuditService.class);
    private final AuditRepository auditRepository;

    public AuditService(AuditRepository auditRepository) {
        this.auditRepository = auditRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logAction(String actionType, String resourceType, Long resourceId, String details) {
        try {
            AuditEntry auditEntry = new AuditEntry();
            auditEntry.setActionType(actionType);
            auditEntry.setResourceType(resourceType);
            auditEntry.setResourceId(resourceId);
            auditEntry.setDetails(details);
            
            // Получаем текущего пользователя
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                auditEntry.setUserEmail(authentication.getName());
            }
            
            auditRepository.save(auditEntry);
        } catch (Exception e) {
            logger.error("Failed to log action: {}", e.getMessage(), e);
            // Не прерываем основной поток выполнения из-за ошибки логирования
        }
    }

    // Методы для удобства использования
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logCreate(String resourceType, Long resourceId, String details) {
        logAction("CREATE", resourceType, resourceId, details);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logUpdate(String resourceType, Long resourceId, String details) {
        logAction("UPDATE", resourceType, resourceId, details);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logDelete(String resourceType, Long resourceId, String details) {
        logAction("DELETE", resourceType, resourceId, details);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logLogin(String userEmail) {
        try {
            AuditEntry auditEntry = new AuditEntry();
            auditEntry.setActionType("LOGIN");
            auditEntry.setUserEmail(userEmail);
            auditRepository.save(auditEntry);
        } catch (Exception e) {
            logger.error("Failed to log login: {}", e.getMessage(), e);
            // Не прерываем основной поток выполнения из-за ошибки логирования
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logLogout(String userEmail) {
        try {
            AuditEntry auditEntry = new AuditEntry();
            auditEntry.setActionType("LOGOUT");
            auditEntry.setUserEmail(userEmail);
            auditRepository.save(auditEntry);
        } catch (Exception e) {
            logger.error("Failed to log logout: {}", e.getMessage(), e);
            // Не прерываем основной поток выполнения из-за ошибки логирования
        }
    }
}
