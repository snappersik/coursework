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
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;

@Service
public class ProductService extends GenericService<Product, ProductDTO> {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final ProductImageService productImageService;

    public ProductService(ProductRepository productRepository,
            ProductMapper productMapper,
            ProductImageService productImageService) {
        super(productRepository, productMapper);
        this.productRepository = productRepository;
        this.productMapper = productMapper;
        this.productImageService = productImageService;
    }

    public Page<ProductDTO> searchProducts(String name, String categoryName, Pageable pageable) {
        ProductCategory category = null;
        if (categoryName != null && !categoryName.isEmpty() && !"all".equalsIgnoreCase(categoryName)) {
            try {
                category = ProductCategory.valueOf(categoryName.toUpperCase());
            } catch (IllegalArgumentException ignored) {
            }
        }
        // Исправление: если name пустой, передавайте пустую строку, а не null
        if (name == null)
            name = "";
        Page<Product> page = productRepository.findAllByNameAndCategory(name, category, pageable);
        List<ProductDTO> dtos = productMapper.toDTOs(page.getContent());
        return new PageImpl<>(dtos, pageable, page.getTotalElements());
    }

    public ProductDTO createProductWithImage(ProductDTO productDTO, MultipartFile file) throws IOException {
        ProductDTO createdProduct = create(productDTO);

        if (file != null && !file.isEmpty()) {
            productImageService.uploadImage(createdProduct.getId(), file);
            // Обновляем DTO после загрузки изображения
            createdProduct = getOne(createdProduct.getId());
        }

        return createdProduct;
    }

    public ProductDTO updateProductWithImage(ProductDTO productDTO, MultipartFile file) throws IOException {
        ProductDTO updatedProduct = update(productDTO);

        if (file != null && !file.isEmpty()) {
            productImageService.uploadImage(updatedProduct.getId(), file);
            // Обновляем DTO после загрузки изображения
            updatedProduct = getOne(updatedProduct.getId());
        }

        return updatedProduct;
    }

    public boolean canDeleteProduct(Long productId) {
        return productRepository.isProductCanBeDeleted(productId);
    }
}
