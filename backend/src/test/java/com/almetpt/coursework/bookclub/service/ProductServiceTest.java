package com.almetpt.coursework.bookclub.service;

import com.almetpt.coursework.bookclub.dto.ProductDTO;
import com.almetpt.coursework.bookclub.mapper.ProductMapper;
import com.almetpt.coursework.bookclub.model.Product;
import com.almetpt.coursework.bookclub.model.ProductCategory;
import com.almetpt.coursework.bookclub.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;
    
    @Mock
    private ProductMapper productMapper;
    
    @InjectMocks
    private ProductService productService;
    
    @Test
    void searchProducts_WhenCategoryProvided_ShouldReturnFilteredProducts() {
        // Arrange
        String name = null;
        String categoryStr = "E_BOOK";
        ProductCategory category = ProductCategory.E_BOOK;
        Pageable pageable = PageRequest.of(0, 10);
        
        Product product1 = new Product();
        product1.setId(1L);
        product1.setName("Java Programming");
        product1.setPrice(new BigDecimal("29.99"));
        product1.setCategory(ProductCategory.E_BOOK);
        
        Product product2 = new Product();
        product2.setId(2L);
        product2.setName("Spring Boot Guide");
        product2.setPrice(new BigDecimal("24.99"));
        product2.setCategory(ProductCategory.E_BOOK);
        
        List<Product> products = Arrays.asList(product1, product2);
        Page<Product> productPage = new PageImpl<>(products, pageable, products.size());
        
        ProductDTO productDTO1 = new ProductDTO();
        productDTO1.setId(1L);
        productDTO1.setName("Java Programming");
        productDTO1.setPrice(new BigDecimal("29.99"));
        productDTO1.setCategory(ProductCategory.E_BOOK);
        
        ProductDTO productDTO2 = new ProductDTO();
        productDTO2.setId(2L);
        productDTO2.setName("Spring Boot Guide");
        productDTO2.setPrice(new BigDecimal("24.99"));
        productDTO2.setCategory(ProductCategory.E_BOOK);
        
        List<ProductDTO> productDTOs = Arrays.asList(productDTO1, productDTO2);
        
        when(productRepository.findAllByNameAndCategory(name, category, pageable)).thenReturn(productPage);
        when(productMapper.toDTOs(products)).thenReturn(productDTOs);
        
        // Act
        Page<ProductDTO> result = productService.searchProducts(name, categoryStr, pageable);
        
        // Assert
        assertEquals(2, result.getContent().size());
        assertEquals("Java Programming", result.getContent().get(0).getName());
        assertEquals("Spring Boot Guide", result.getContent().get(1).getName());
        assertEquals(ProductCategory.E_BOOK, result.getContent().get(0).getCategory());
    }
}
