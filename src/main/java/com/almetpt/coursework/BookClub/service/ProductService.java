package com.almetpt.coursework.BookClub.service;

import com.almetpt.coursework.BookClub.constants.Errors;
import com.almetpt.coursework.BookClub.dto.ProductDTO;
import com.almetpt.coursework.BookClub.exception.MyDeleteException;
import com.almetpt.coursework.BookClub.mapper.ProductMapper;
import com.almetpt.coursework.BookClub.model.Product;
import com.almetpt.coursework.BookClub.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.webjars.NotFoundException;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ProductService extends GenericService<Product, ProductDTO> {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    @Autowired
    public ProductService(ProductRepository productRepository, ProductMapper productMapper) {
        super(productRepository, productMapper);
        this.productRepository = productRepository;
        this.productMapper = productMapper;
    }

    public Page<ProductDTO> getAllProducts(Pageable pageable) {
        Page<Product> productsPaginated = productRepository.findAll(pageable);
        List<ProductDTO> result = productsPaginated.map(productMapper::toDTO).getContent();
        return new PageImpl<>(result, pageable, productsPaginated.getTotalElements());
    }

    public Page<ProductDTO> searchProducts(String name, String category, Pageable pageRequest) {
        Page<Product> productsPaginated = productRepository.findAllByProductNameAndCategory(name, category, pageRequest);
        List<ProductDTO> result = productsPaginated.map(productMapper::toDTO).getContent();
        return new PageImpl<>(result, pageRequest, productsPaginated.getTotalElements());
    }

    public void deleteSoft(final Long id) throws MyDeleteException {
        Product product = productRepository.findById(id).orElseThrow(
                () -> new NotFoundException("Продукт не найден"));
        boolean productCanBeDeleted = productRepository.isProductCanBeDeleted(id);
        if (productCanBeDeleted) {
            markAsDeleted(product);
            productRepository.save(product);
        } else {
            throw new MyDeleteException(Errors.Products.PRODUCT_DELETED_ERROR);
        }
    }


    private void markAsDeleted(Product product) {
        product.setDeleted(true);
        product.setDeletedWhen(LocalDateTime.now());
    }
}
