package com.almetpt.coursework.bookclub.aspects;

import com.almetpt.coursework.bookclub.annotations.AdminAction;
import com.almetpt.coursework.bookclub.controllers.CartController;
import com.almetpt.coursework.bookclub.model.AuditEntry;
import com.almetpt.coursework.bookclub.repository.AuditRepository;
import com.almetpt.coursework.bookclub.repository.CartRepository;
import com.almetpt.coursework.bookclub.model.Cart;
import com.almetpt.coursework.bookclub.service.userdetails.CustomUserDetails;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;

@Aspect
@Component
@Order(1)
public class AdminActionLoggerAspect {
    
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final AuditRepository auditRepository;
    private final CartRepository cartRepository;
    
    public AdminActionLoggerAspect(AuditRepository auditRepository, CartRepository cartRepository) {
        this.auditRepository = auditRepository;
        this.cartRepository = cartRepository;
    }
    
    @Around("@annotation(adminAction)")
    public Object logAdminAction(ProceedingJoinPoint joinPoint, AdminAction adminAction) throws Throwable {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return joinPoint.proceed();
        }
        
        String userEmail = authentication.getName();
        String userIdentifier;
        
        // Проверяем, является ли пользователь администратором
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        
        if (isAdmin) {
            userIdentifier = "ADMIN"; // Для администратора используем строку "ADMIN"
        } else if (authentication.getPrincipal() instanceof CustomUserDetails) {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            if (userDetails.getUserId() != null) {
                userIdentifier = userDetails.getUserId().toString();
            } else {
                userIdentifier = "UNKNOWN";
            }
        } else {
            userIdentifier = "UNKNOWN";
        }
        
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        
        // Получаем детальную информацию о параметрах метода
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String[] parameterNames = signature.getParameterNames();
        Object[] args = joinPoint.getArgs();
        
        StringBuilder paramsWithNames = new StringBuilder();
        if (parameterNames != null && parameterNames.length > 0) {
            for (int i = 0; i < parameterNames.length; i++) {
                if (i > 0) {
                    paramsWithNames.append(", ");
                }
                paramsWithNames.append(parameterNames[i]).append("=");
                if (args[i] != null) {
                    // Ограничиваем длину значения параметра
                    String argValue = args[i].toString();
                    if (argValue.length() > 30) {
                        argValue = argValue.substring(0, 27) + "...";
                    }
                    paramsWithNames.append(argValue);
                } else {
                    paramsWithNames.append("null");
                }
            }
        }
        
        LocalDateTime now = LocalDateTime.now();
        
        long start = System.currentTimeMillis();
        Object result = joinPoint.proceed();
        long elapsedTime = System.currentTimeMillis() - start;
        
        logger.info("[ADMIN ACTION] User: {} | Class: {} | Method: {} | Params: {} | Time: {}ms | Timestamp: {}",
                userEmail, className, methodName, paramsWithNames, elapsedTime, now);
        
        // Проверяем, нужно ли логировать действие
        if (shouldLogAction(methodName, joinPoint, isAdmin)) {
            AuditEntry.ActionType actionType = getActionTypeForMethod(methodName, joinPoint);
            if (actionType != null) {
                // Формируем детали и ограничиваем их длину
                String detailsText = "Class: %s, Method: %s, Params: %s, Time: %dms".formatted(
                        className, methodName, paramsWithNames, elapsedTime);
                
                // Ограничиваем длину до 200 символов
                if (detailsText.length() > 197) {
                    detailsText = detailsText.substring(0, 197) + "...";
                }
                
                AuditEntry auditEntry = AuditEntry.builder()
                        .actionType(actionType)
                        .userIdentifier(userIdentifier)
                        .timestamp(now)
                        .details(detailsText)
                        .build();
                auditRepository.save(auditEntry);
            }
        }
        
        return result;
    }
    
    private boolean shouldLogAction(String methodName, JoinPoint joinPoint, boolean isAdmin) {
        // Всегда логируем действия администратора
        if (isAdmin) {
            return true;
        }
        
        // Проверяем, является ли это просмотром корзины
        if (methodName.equals("getById") && joinPoint.getTarget() instanceof CartController) {
            // Получаем ID пользователя
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Long userId = null;
            if (authentication.getPrincipal() instanceof CustomUserDetails) {
                CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
                if (userDetails.getUserId() != null) {
                    userId = userDetails.getUserId().longValue();
                }
            }
            
            // Получаем ID корзины из аргументов метода
            Long cartId = null;
            Object[] args = joinPoint.getArgs();
            if (args.length > 0 && args[0] instanceof Long long1) {
                cartId = long1;
            }
            
            // Проверяем, принадлежит ли корзина текущему пользователю
            if (userId != null && cartId != null) {
                Optional<Cart> cartOptional = cartRepository.findById(cartId);
                if (cartOptional.isPresent() && cartOptional.get().getUser().getId().equals(userId)) {
                    return false; // Не логируем, если пользователь просматривает свою корзину
                }
            }
        }
        
        return true; // Логируем все остальные действия
    }
    
    private AuditEntry.ActionType getActionTypeForMethod(String methodName, JoinPoint joinPoint) {
        Object target = joinPoint.getTarget();
        
        if (methodName.startsWith("create")) {
            return AuditEntry.ActionType.CREATE_EVENT;
        } else if (methodName.startsWith("delete")) {
            return AuditEntry.ActionType.DELETE_EVENT;
        } else if (methodName.contains("cancel")) {
            return AuditEntry.ActionType.CANCEL_EVENT;
        } else if (methodName.contains("reschedule")) {
            return AuditEntry.ActionType.RESCHEDULE_EVENT;
        } else if (methodName.contains("EventApplication")) {
            return AuditEntry.ActionType.UPDATE_EVENT_APPLICATION;
        } else if (methodName.contains("User")) {
            return AuditEntry.ActionType.MANAGE_USER;
        } else if (methodName.equals("getById") && target instanceof CartController) {
            return AuditEntry.ActionType.VIEW_CART;
        } else {
            return null;
        }
    }
}
