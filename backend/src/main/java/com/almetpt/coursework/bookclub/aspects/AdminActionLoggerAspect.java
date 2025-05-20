package com.almetpt.coursework.bookclub.aspects;

import com.almetpt.coursework.bookclub.annotations.AdminAction;
import com.almetpt.coursework.bookclub.model.AuditEntry;
import com.almetpt.coursework.bookclub.model.Cart;
import com.almetpt.coursework.bookclub.repository.AuditRepository;
import com.almetpt.coursework.bookclub.repository.CartRepository;
import com.almetpt.coursework.bookclub.service.userdetails.CustomUserDetails;
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
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

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
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal().toString())) {
            return joinPoint.proceed();
        }

        String actualUserEmail = authentication.getName();
        String loggingUserIdentifier;
        boolean isActualAdminRole = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        
        if (isActualAdminRole) {
            loggingUserIdentifier = "ADMIN_ROLE";
        } else if (authentication.getPrincipal() instanceof CustomUserDetails) {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            if (userDetails.getUserId() != null) {
                loggingUserIdentifier = "USER_ID:" + userDetails.getUserId().toString();
            } else {
                loggingUserIdentifier = "UNKNOWN_USER_DETAILS_ID";
            }
        } else {
            loggingUserIdentifier = "UNKNOWN_PRINCIPAL_TYPE";
        }

        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();
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
                    String argValue = args[i].toString();
                    if (args[i] instanceof MultipartFile) {
                        argValue = "MultipartFile[" + ((MultipartFile) args[i]).getOriginalFilename() + "]";
                    }
                    if (argValue.length() > 50) {
                        argValue = argValue.substring(0, 47) + "...";
                    }
                    paramsWithNames.append(argValue);
                } else {
                    paramsWithNames.append("null");
                }
            }
        }

        LocalDateTime now = LocalDateTime.now();
        long start = System.currentTimeMillis();
        Object result;
        
        try {
            result = joinPoint.proceed();
        } catch (Throwable throwable) {
            long elapsedTimeOnError = System.currentTimeMillis() - start;
            logger.error("[ADMIN ACTION FAILED] UserEmail: {} (Identifier: {}) | Class: {} | Method: {} | Params: {} | Time: {}ms | Timestamp: {} | Error: {}",
                    actualUserEmail, loggingUserIdentifier, className, methodName, paramsWithNames.toString(), elapsedTimeOnError, now, throwable.getMessage());
            throw throwable;
        }

        long elapsedTime = System.currentTimeMillis() - start;
        logger.info("[ADMIN ACTION SUCCEEDED] UserEmail: {} (Identifier: {}) | Class: {} | Method: {} | Params: {} | Time: {}ms | Timestamp: {}",
                actualUserEmail, loggingUserIdentifier, className, methodName, paramsWithNames.toString(), elapsedTime, now);
        
        if (shouldLogActionToDb(methodName, className, joinPoint, isActualAdminRole, actualUserEmail)) {
            try {
                saveAuditEntry(methodName, className, joinPoint, loggingUserIdentifier, actualUserEmail, paramsWithNames.toString(), elapsedTime, now, args);
            } catch (Exception e) {
                logger.error("Failed to save audit entry: {}", e.getMessage(), e);
                // Не прерываем основной поток выполнения из-за ошибки логирования
            }
        }

        return result;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    protected void saveAuditEntry(String methodName, String className, ProceedingJoinPoint joinPoint, 
                                String loggingUserIdentifier, String actualUserEmail, String paramsWithNames, 
                                long elapsedTime, LocalDateTime now, Object[] args) {
        String determinedActionType = getActionTypeForMethod(methodName, className, joinPoint);
        if (determinedActionType != null) {
            String detailsText = "Performed by: %s. Class: %s, Method: %s, Params: %s, ExecTime: %dms".formatted(
                    loggingUserIdentifier, className, methodName, paramsWithNames, elapsedTime);
            
            if (detailsText.length() > 990) {
                detailsText = detailsText.substring(0, 987) + "...";
            }

            AuditEntry auditEntry = AuditEntry.builder()
                    .actionType(determinedActionType)
                    .userEmail(actualUserEmail)
                    .resourceType(extractResourceType(className))
                    .resourceId(extractResourceId(args))
                    .timestamp(now)
                    .details(detailsText)
                    .build();
            
            try {
                auditRepository.save(auditEntry);
            } catch (Exception e) {
                logger.error("AdminActionLoggerAspect: Failed to save audit entry to DB. Entry: {}, Error: {}", auditEntry.toString(), e.getMessage(), e);
            }
        }
    }

    private boolean shouldLogActionToDb(String methodName, String className, ProceedingJoinPoint joinPoint, boolean isActualAdminRole, String currentUserEmail) {
        if (isActualAdminRole) {
            return true;
        }

        if ("CartController".equals(className) && "getById".equals(methodName)) {
            Object[] args = joinPoint.getArgs();
            if (args.length > 0 && args[0] instanceof Long) {
                Long cartId = (Long) args[0];
                Optional<Cart> cartOptional = cartRepository.findById(cartId);
                if (cartOptional.isPresent() && cartOptional.get().getUser().getEmail().equals(currentUserEmail) && !isActualAdminRole) {
                    return false;
                }
            }
        }
        
        return true;
    }

    private String getActionTypeForMethod(String methodName, String className, ProceedingJoinPoint joinPoint) {
        String resourceType = extractResourceType(className).toUpperCase();
        
        if (methodName.startsWith("create") || methodName.contains("Create") || methodName.startsWith("add")) {
            return "CREATE_" + resourceType;
        } else if (methodName.startsWith("delete") || methodName.contains("Delete") || methodName.startsWith("remove") || methodName.startsWith("softDelete")) {
            return "DELETE_" + resourceType;
        } else if (methodName.startsWith("update") || methodName.contains("Update") || methodName.startsWith("edit") || methodName.startsWith("set")) {
            return "UPDATE_" + resourceType;
        } else if (methodName.contains("cancel") || methodName.contains("Cancel")) {
            return "CANCEL_" + resourceType;
        } else if (methodName.contains("reschedule") || methodName.contains("Reschedule")) {
            return "RESCHEDULE_" + resourceType;
        } else if (methodName.contains("approve") || methodName.contains("Approve")) {
            return "APPROVE_" + resourceType;
        } else if (methodName.contains("reject") || methodName.contains("Reject")) {
            return "REJECT_" + resourceType;
        } else if (methodName.contains("mark") || methodName.contains("Mark")) {
            return "MARK_" + resourceType;
        } else if (methodName.startsWith("get") || methodName.startsWith("view") || methodName.startsWith("list") || methodName.startsWith("find") || methodName.startsWith("search")) {
            return "VIEW_" + resourceType;
        } else if (methodName.equalsIgnoreCase("login")) {
            return "LOGIN";
        } else if (methodName.equalsIgnoreCase("logout")) {
            return "LOGOUT";
        } else if (methodName.equalsIgnoreCase("registerUser")) {
            return "REGISTER";
        }
        
        return "ADMIN_ACTION_" + resourceType;
    }

    private String extractResourceType(String className) {
        if (className.contains("BookController")) return "BOOK";
        if (className.contains("UserController")) return "USER";
        if (className.contains("OrderController")) return "ORDER";
        if (className.contains("EventApplicationController")) return "EVENT_APPLICATION";
        if (className.contains("EventController")) return "EVENT";
        if (className.contains("CartController")) return "CART";
        if (className.contains("SliderController")) return "SLIDER";
        if (className.contains("ProductController")) return "PRODUCT";
        if (className.contains("AuthController")) return "AUTH";
        
        if (className.endsWith("Service")) {
            return className.substring(0, className.length() - "Service".length());
        }
        
        if (className.endsWith("Controller")) {
            return className.substring(0, className.length() - "Controller".length());
        }
        
        return className;
    }

    private Long extractResourceId(Object[] args) {
        if (args != null && args.length > 0) {
            if (args[0] instanceof Long) {
                return (Long) args[0];
            }
        }
        return null;
    }
}
