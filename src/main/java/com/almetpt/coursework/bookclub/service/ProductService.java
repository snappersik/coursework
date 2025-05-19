package com.almetpt.coursework.bookclub.service;

import com.almetpt.coursework.bookclub.dto.ProductDTO;
import com.almetpt.coursework.bookclub.mapper.ProductMapper;
import com.almetpt.coursework.bookclub.model.Product;
import com.almetpt.coursework.bookclub.model.ProductCategory;
import com.almetpt.coursework.bookclub.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService extends GenericService<Product, ProductDTO> {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    public ProductService(ProductRepository productRepository,
                          ProductMapper productMapper) {
        super(productRepository, productMapper);
        this.productRepository = productRepository;
        this.productMapper = productMapper;
    }

    public Page<ProductDTO> searchProducts(String name, String categoryName, Pageable pageable) {
        ProductCategory category = null;
        if (categoryName != null) {
            try {
                category = ProductCategory.valueOf(categoryName);
            } catch (IllegalArgumentException ignored) { }
        }

        Page<Product> page = productRepository.findAllByNameAndCategory(name, category, pageable);
        List<ProductDTO> dtos = productMapper.toDTOs(page.getContent());
        return new PageImpl<>(dtos, pageable, page.getTotalElements());
    }
}
