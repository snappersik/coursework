package com.almetpt.coursework.bookclub.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class UserModelTest {

    @Test
    void getFullName_ShouldReturnFormattedFullName() {
        // Arrange
        User user = new User();
        user.setFirstName("Иван");
        user.setLastName("Петров");
        user.setPatronymic("Сергеевич");
        
        // Act
        String fullName = user.getFullName();
        
        // Assert
        assertEquals("Петров Иван Сергеевич", fullName);
    }
    
    @Test
    void getFullName_WithNullPatronymic_ShouldReturnNameWithoutPatronymic() {
        // Arrange
        User user = new User();
        user.setFirstName("Иван");
        user.setLastName("Петров");
        user.setPatronymic(null);
        
        // Act
        String fullName = user.getFullName();
        
        // Assert
        assertEquals("Петров Иван null", fullName);
    }
}
