package com.almetpt.coursework.bookclub.service;

import com.almetpt.coursework.bookclub.constants.Errors;
import com.almetpt.coursework.bookclub.dto.ProductDTO;
import com.almetpt.coursework.bookclub.exception.MyDeleteException;
import com.almetpt.coursework.bookclub.mapper.ProductMapper;
import com.almetpt.coursework.bookclub.model.Product;
import com.almetpt.coursework.bookclub.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.webjars.NotFoundException;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ProductService extends GenericService<Product, ProductDTO> {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    public ProductService(ProductRepository productRepository, ProductMapper productMapper) {
        super(productRepository, productMapper);
        this.productRepository = productRepository;
        this.productMapper = productMapper;
    }

    @Override
    public List<ProductDTO> listAll() {
        return productMapper.toDTOs(productRepository.findAll());
    }

    @Override
    public List<ProductDTO> listAllNotDeleted() {
        return productMapper.toDTOs(productRepository.findAllByIsDeletedFalse());
    }

    public Page<ProductDTO> searchProducts(String name, String category, Pageable pageRequest) {
        Page<Product> productsPaginated = productRepository.findAllByNameAndCategory(name, category, pageRequest);
        List<ProductDTO> result = productMapper.toDTOs(productsPaginated.getContent());
        return new PageImpl<>(result, pageRequest, productsPaginated.getTotalElements());
    }

    @Override
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

    protected NotFoundException createNotFoundException(Long id) {
        return new NotFoundException(Errors.Products.PRODUCT_NOT_FOUND_ERROR.formatted(id));
    }

    private void markAsDeleted(Product product) {
        product.setDeleted(true);
        product.setDeletedWhen(LocalDateTime.now());
    }
}
