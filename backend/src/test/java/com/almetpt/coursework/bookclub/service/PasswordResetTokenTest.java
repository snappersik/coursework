package com.almetpt.coursework.bookclub.service;

import com.almetpt.coursework.bookclub.model.User;
import com.almetpt.coursework.bookclub.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PasswordResetTokenTest {

    @Mock
    private UserRepository userRepository;
    
    @InjectMocks
    private UserService userService;
    
    @Test
    void isPasswordResetTokenValid_WhenTokenIsValid_ShouldReturnTrue() {
        // Arrange
        String token = "valid-token";
        User user = new User();
        user.setEmail("user@example.com");
        user.setChangePasswordToken(token);
        user.setUpdatedWhen(LocalDateTime.now().minusHours(1)); // Token created 1 hour ago
        
        when(userRepository.findUserByChangePasswordToken(token)).thenReturn(user);
        
        // Act
        boolean result = userService.isPasswordResetTokenValid(token);
        
        // Assert
        assertTrue(result);
    }
    
    @Test
    void isPasswordResetTokenValid_WhenTokenIsExpired_ShouldReturnFalse() {
        // Arrange
        String token = "expired-token";
        User user = new User();
        user.setEmail("user@example.com");
        user.setChangePasswordToken(token);
        user.setUpdatedWhen(LocalDateTime.now().minusHours(25)); // Token created 25 hours ago (expired)
        
        when(userRepository.findUserByChangePasswordToken(token)).thenReturn(user);
        
        // Act
        boolean result = userService.isPasswordResetTokenValid(token);
        
        // Assert
        assertFalse(result);
    }
    
    @Test
    void isPasswordResetTokenValid_WhenTokenDoesNotExist_ShouldReturnFalse() {
        // Arrange
        String token = "non-existent-token";
        when(userRepository.findUserByChangePasswordToken(token)).thenReturn(null);
        
        // Act
        boolean result = userService.isPasswordResetTokenValid(token);
        
        // Assert
        assertFalse(result);
    }
}
