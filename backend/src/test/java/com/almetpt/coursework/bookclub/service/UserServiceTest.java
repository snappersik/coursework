package com.almetpt.coursework.bookclub.service;

import com.almetpt.coursework.bookclub.dto.UserDTO;
import com.almetpt.coursework.bookclub.mapper.UserMapper;
import com.almetpt.coursework.bookclub.model.User;
import com.almetpt.coursework.bookclub.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    
    @Mock
    private UserMapper userMapper;
    
    @InjectMocks
    private UserService userService;
    
    @Test
    void getUserByEmail_WhenUserExists_ShouldReturnUserDTO() {
        // Arrange
        String email = "test@example.com";
        User user = new User();
        user.setId(1L);
        user.setEmail(email);
        user.setFirstName("Test");
        user.setLastName("User");
        user.setBirthDate(LocalDate.of(1990, 1, 1));
        
        UserDTO userDTO = new UserDTO();
        userDTO.setId(1L);
        userDTO.setEmail(email);
        userDTO.setFirstName("Test");
        userDTO.setLastName("User");
        userDTO.setBirthDate(LocalDate.of(1990, 1, 1));
        
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(userMapper.toDTO(user)).thenReturn(userDTO);
        
        // Act
        UserDTO result = userService.getUserByEmail(email);
        
        // Assert
        assertEquals(email, result.getEmail());
        assertEquals("Test", result.getFirstName());
        assertEquals("User", result.getLastName());
    }
    
    @Test
    void getUserByEmail_WhenUserDoesNotExist_ShouldReturnNull() {
        // Arrange
        String email = "nonexistent@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
        
        // Act
        UserDTO result = userService.getUserByEmail(email);
        
        // Assert
        assertNull(result);
    }
}
