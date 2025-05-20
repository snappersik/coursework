package com.almetpt.coursework.bookclub.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ProductImageTest {

    @Test
    void hasLocalImage_WhenImageDataExists_ShouldReturnTrue() {
        // Arrange
        Product product = new Product();
        product.setCoverImageData(new byte[]{1, 2, 3, 4, 5});
        
        // Act
        boolean result = product.hasLocalImage();
        
        // Assert
        assertTrue(result);
    }
    
    @Test
    void hasLocalImage_WhenImageDataIsNull_ShouldReturnFalse() {
        // Arrange
        Product product = new Product();
        product.setCoverImageData(null);
        
        // Act
        boolean result = product.hasLocalImage();
        
        // Assert
        assertFalse(result);
    }
    
    @Test
    void hasLocalImage_WhenImageDataIsEmpty_ShouldReturnFalse() {
        // Arrange
        Product product = new Product();
        product.setCoverImageData(new byte[0]);
        
        // Act
        boolean result = product.hasLocalImage();
        
        // Assert
        assertFalse(result);
    }
}
