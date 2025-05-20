package com.almetpt.coursework.bookclub.controllers;

import com.almetpt.coursework.bookclub.model.Product;
import com.almetpt.coursework.bookclub.model.ProductCategory;
import com.almetpt.coursework.bookclub.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class ProductSearchIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ProductRepository productRepository;
    
    @BeforeEach
    void setUp() {
        // Очистка и подготовка тестовых данных
        productRepository.deleteAll();
        
        // Создание тестовых продуктов
        Product product1 = new Product();
        product1.setName("Java Programming");
        product1.setDescription("Learn Java programming");
        product1.setPrice(new BigDecimal("29.99"));
        product1.setCategory(ProductCategory.E_BOOK);
        product1.setCreatedWhen(LocalDateTime.now());
        productRepository.save(product1);
        
        Product product2 = new Product();
        product2.setName("Spring Boot Guide");
        product2.setDescription("Comprehensive guide to Spring Boot");
        product2.setPrice(new BigDecimal("24.99"));
        product2.setCategory(ProductCategory.E_BOOK);
        product2.setCreatedWhen(LocalDateTime.now());
        productRepository.save(product2);
        
        Product product3 = new Product();
        product3.setName("Java Design Patterns");
        product3.setDescription("Audio book about design patterns in Java");
        product3.setPrice(new BigDecimal("19.99"));
        product3.setCategory(ProductCategory.AUDIO_BOOK);
        product3.setCreatedWhen(LocalDateTime.now());
        productRepository.save(product3);
    }
    
    @Test
    @WithMockUser(roles = "USER")
    void searchProducts_WhenNameContainsJava_ShouldReturnMatchingProducts() throws Exception {
        mockMvc.perform(get("/api/rest/products/search")
                .param("name", "Java")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].name").value("Java Programming"))
                .andExpect(jsonPath("$.content[1].name").value("Java Design Patterns"));
    }
    
    @Test
    @WithMockUser(roles = "USER")
    void searchProducts_WhenCategoryIsEBook_ShouldReturnEBooks() throws Exception {
        mockMvc.perform(get("/api/rest/products/search")
                .param("category", "E_BOOK")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].category").value("E_BOOK"))
                .andExpect(jsonPath("$.content[1].category").value("E_BOOK"));
    }
}
