package com.almetpt.coursework.bookclub.aspects;

import com.almetpt.coursework.bookclub.model.AuditEntry;
import com.almetpt.coursework.bookclub.model.Cart;
import com.almetpt.coursework.bookclub.repository.AuditRepository;
import com.almetpt.coursework.bookclub.repository.CartRepository;
import com.almetpt.coursework.bookclub.service.userdetails.CustomUserDetails;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

@Aspect
@Component
@Order(2)
public class AuditAspect {

    private static final Logger log = LoggerFactory.getLogger(AuditAspect.class);
    private final AuditRepository auditRepository;
    private final CartRepository cartRepository;

    public AuditAspect(AuditRepository auditRepository, CartRepository cartRepository) {
        this.auditRepository = auditRepository;
        this.cartRepository = cartRepository;
    }

    private String determineActionType(String methodName, String className) {
        if (methodName.startsWith("create") || methodName.startsWith("add") || methodName.startsWith("register") || methodName.startsWith("upload")) {
            return "CREATE";
        } else if (methodName.startsWith("update") || methodName.startsWith("set") || methodName.startsWith("reschedule") || methodName.startsWith("mark") || methodName.startsWith("change") || methodName.startsWith("edit")) {
            return "UPDATE";
        } else if (methodName.startsWith("delete") || methodName.startsWith("remove") || methodName.startsWith("cancel") || methodName.startsWith("softDelete")) {
            return "DELETE";
        } else if (methodName.startsWith("get") || methodName.startsWith("find") || methodName.startsWith("list") || methodName.startsWith("search") || methodName.startsWith("view")) {
            return "VIEW";
        }
        // Specific common patterns
        if (methodName.contains("Login")) return "LOGIN";
        if (methodName.contains("Logout")) return "LOGOUT";
        
        log.warn("Could not determine a clear action type for method: {} in class: {}", methodName, className);
        return "OPERATION"; // Generic fallback
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
        if (className.contains("AuthController")) return "AUTH"; // For login/register
        
        // Fallback by removing "Controller" suffix
        if (className.endsWith("Controller")) {
            return className.substring(0, className.length() - "Controller".length()).toUpperCase();
        }
        return className.toUpperCase();
    }

    // Pointcut for various controller actions. Refine as needed.
    @AfterReturning(pointcut = "execution(* com.almetpt.coursework.bookclub.controllers.EventController.*(..)) || " +
            "execution(* com.almetpt.coursework.bookclub.controllers.EventApplicationController.*(..)) || " +
            "execution(* com.almetpt.coursework.bookclub.controllers.UserController.*(..)) || " +
            "execution(* com.almetpt.coursework.bookclub.controllers.BookController.*(..)) || " + // Added BookController
            "execution(* com.almetpt.coursework.bookclub.controllers.ProductController.*(..)) || " + // Added ProductController
            "execution(* com.almetpt.coursework.bookclub.controllers.OrderController.*(..)) || " + // Added OrderController
            "execution(* com.almetpt.coursework.bookclub.controllers.SliderController.*(..)) || " + // Added SliderController
            "execution(* com.almetpt.coursework.bookclub.controllers.CartController.getById(..)) || " + // Kept specific CartController method
            "execution(* com.almetpt.coursework.bookclub.controllers.AuthController.login(..)) || " + // For login
            "execution(* com.almetpt.coursework.bookclub.controllers.AuthController.registerUser(..)) || " + // For registration
            "execution(* com.almetpt.coursework.bookclub.controllers.AuthController.logout(..))", // For logout
            returning = "result")
    public void audit(JoinPoint joinPoint, Object result) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal().toString())) {
            return; 
        }

        String userEmail;
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (authentication.getPrincipal() instanceof CustomUserDetails) {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            userEmail = userDetails.getUsername(); // email
        } else {
            userEmail = authentication.getName();
        }

        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();

        if (!shouldLogAction(methodName, className, joinPoint, isAdmin, userEmail)) {
            log.trace("AuditAspect: Action for method {} in {} by user {} not logged due to shouldLogAction rules.", methodName, className, userEmail);
            return;
        }

        String actionTypeString = determineActionType(methodName, className);
        String resourceTypeString = extractResourceType(className);

        Long resourceId = null;
        Object[] args = joinPoint.getArgs();
        if (args != null && args.length > 0) {
            // Try to find an ID if it's the first argument and a Long
            if (args[0] instanceof Long) {
                resourceId = (Long) args[0];
            }
        }


        String resultStr = result != null ? result.toString() : "null";
        if (resultStr.length() > 250) {
            resultStr = resultStr.substring(0, 247) + "...";
        }
        
        String argsString = Arrays.stream(args)
                                  .map(arg -> {
                                      if (arg instanceof MultipartFile) {
                                          return "MultipartFile[" + ((MultipartFile) arg).getOriginalFilename() + "]";
                                      }
                                      return arg != null ? arg.toString() : "null";
                                  })
                                  .map(s -> s.length() > 50 ? s.substring(0, 47) + "..." : s)
                                  .collect(Collectors.joining(", "));

        String details = String.format("Class: %s, Method: %s, Args: [%s], Result: %s", className, methodName, argsString, resultStr);
        if (details.length() > 990) {
            details = details.substring(0, 987) + "...";
        }

        AuditEntry auditEntry = AuditEntry.builder()
                .actionType(actionTypeString)
                .userEmail(userEmail)
                .resourceType(resourceTypeString)
                .resourceId(resourceId)
                .timestamp(LocalDateTime.now())
                .details(details)
                .build();
        try {
            auditRepository.save(auditEntry);
            log.debug("AuditAspect: Saved audit entry: {}", auditEntry);
        } catch (Exception e) {
            log.error("AuditAspect: Failed to save audit entry. Entry: {}, Error: {}", auditEntry, e.getMessage(), e);
        }
    }

    private boolean shouldLogAction(String methodName, String className, JoinPoint joinPoint, boolean isAdmin, String currentUserEmail) {
        if (isAdmin && !"ADMIN".equalsIgnoreCase(currentUserEmail)) {
             log.trace("AuditAspect: Admin action by {} is being logged.", currentUserEmail);
            return true;
        }
        
        if ("CartController".equals(className) && "getById".equals(methodName)) {
            if (joinPoint.getArgs().length > 0 && joinPoint.getArgs()[0] instanceof Long) {
                Long cartId = (Long) joinPoint.getArgs()[0];
                Optional<Cart> cartOptional = cartRepository.findById(cartId);
                if (cartOptional.isPresent() && cartOptional.get().getUser().getEmail().equals(currentUserEmail)) {
                    log.trace("AuditAspect: Skipping log for user {} viewing their own cart id {}.", currentUserEmail, cartId);
                    return false; 
                }
            }
        }
        
        if ("UserController".equals(className) && "getCurrentUserProfile".equals(methodName) && !isAdmin) {
            log.trace("AuditAspect: Skipping log for user {} viewing their own profile.", currentUserEmail);
            return false;
        }

        return isAdmin || !("CartController".equals(className) && "getById".equals(methodName)) && 
               !("UserController".equals(className) && "getCurrentUserProfile".equals(methodName));
    }
}
