package com.almetpt.coursework.bookclub.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ProductCategoryTest {

    @Test
    void testProductCategoryValues() {
        // Arrange & Act & Assert
        assertEquals("Аудиокнига", ProductCategory.AUDIO_BOOK.getDescription());
        assertEquals("Электронная книга", ProductCategory.E_BOOK.getDescription());
    }
    
    @Test
    void testProductCategoryCount() {
        // Arrange & Act & Assert
        assertEquals(4, ProductCategory.values().length);
    }
}
