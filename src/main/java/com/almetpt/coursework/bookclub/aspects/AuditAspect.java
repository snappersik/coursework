package com.almetpt.coursework.bookclub.aspects;

import com.almetpt.coursework.bookclub.controllers.CartController;
import com.almetpt.coursework.bookclub.model.AuditEntry;
import com.almetpt.coursework.bookclub.repository.AuditRepository;
import com.almetpt.coursework.bookclub.repository.CartRepository;
import com.almetpt.coursework.bookclub.model.Cart;
import com.almetpt.coursework.bookclub.service.userdetails.CustomUserDetails;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;

@Aspect
@Component
@Order(2)
public class AuditAspect {

    private final AuditRepository auditRepository;
    private final CartRepository cartRepository;

    public AuditAspect(AuditRepository auditRepository, CartRepository cartRepository) {
        this.auditRepository = auditRepository;
        this.cartRepository = cartRepository;
    }

    @AfterReturning(pointcut = "execution(* com.almetpt.coursework.bookclub.controllers.EventController.create(..)) || " +
            "execution(* com.almetpt.coursework.bookclub.controllers.EventController.delete(..)) || " +
            "execution(* com.almetpt.coursework.bookclub.controllers.*.softDelete(..)) || " +
            "execution(* com.almetpt.coursework.bookclub.controllers.EventController.cancelEvent(..)) || " +
            "execution(* com.almetpt.coursework.bookclub.controllers.EventController.rescheduleEvent(..)) || " +
            "execution(* com.almetpt.coursework.bookclub.controllers.EventApplicationController.*(..)) || " +
            "execution(* com.almetpt.coursework.bookclub.controllers.UserController.*(..)) || " +
            "execution(* com.almetpt.coursework.bookclub.controllers.CartController.getById(..))", returning = "result")
    public void audit(JoinPoint joinPoint, Object result) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            String userIdentifier;
            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            
            if (isAdmin) {
                userIdentifier = "ADMIN";
            } else if (authentication.getPrincipal() instanceof CustomUserDetails) {
                CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
                if (userDetails.getUserId() != null) {
                    userIdentifier = userDetails.getUserId().toString();
                } else {
                    return; // Пропускаем аудит, если нет userId
                }
            } else {
                return; // Пропускаем аудит, если нет данных о пользователе
            }
            
            String methodName = joinPoint.getSignature().getName();
            String className = joinPoint.getTarget().getClass().getSimpleName();
            
            // Добавляем отладочное логирование
            System.out.println("DEBUG: Method: " + methodName + ", Class: " + className);
            System.out.println("DEBUG: Action type: " + getActionType(methodName, joinPoint));
            
            // Проверяем, нужно ли логировать действие
            if (!shouldLogAction(methodName, joinPoint, isAdmin)) {
                return;
            }
            
            AuditEntry.ActionType actionType = getActionType(methodName, joinPoint);
            
            if (actionType != null) {
                // Ограничиваем длину результата
                String resultStr = result != null ? result.toString() : "null";
                if (resultStr.length() > 100) {
                    resultStr = resultStr.substring(0, 97) + "...";
                }
                
                String details = "Method: %s, Result: %s".formatted(methodName, resultStr);
                
                if (details.length() > 197) {
                    details = details.substring(0, 197) + "...";
                }
                
                AuditEntry auditEntry = AuditEntry.builder()
                        .actionType(actionType)
                        .userIdentifier(userIdentifier)
                        .timestamp(LocalDateTime.now())
                        .details(details)
                        .build();
                auditRepository.save(auditEntry);
            }
        }
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

    private AuditEntry.ActionType getActionType(String methodName, JoinPoint joinPoint) {
        Object target = joinPoint.getTarget();
        
        if (methodName.startsWith("create")) {
            return AuditEntry.ActionType.CREATE_EVENT;
        } else if (methodName.startsWith("delete")) {
            return AuditEntry.ActionType.DELETE_EVENT;
        } else if (methodName.equals("softDelete")) {
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
