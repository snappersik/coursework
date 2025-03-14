package com.almetpt.coursework.BookClub.service;

import com.almetpt.coursework.BookClub.constants.Errors;
import com.almetpt.coursework.BookClub.dto.ProductDTO;
import com.almetpt.coursework.BookClub.exception.MyDeleteException;
import com.almetpt.coursework.BookClub.mapper.ProductMapper;
import com.almetpt.coursework.BookClub.model.Product;
import com.almetpt.coursework.BookClub.repository.ProductRepository;
import com.almetpt.coursework.BookClub.utils.FileHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.webjars.NotFoundException;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    @Autowired
    public ProductService(ProductRepository productRepository, ProductMapper productMapper) {
        this.productRepository = productRepository;
        this.productMapper = productMapper;
    }

    public ProductDTO create(final ProductDTO newProductDTO, MultipartFile file) {
        String fileName = FileHelper.createFile(file); // Сохранение файла и получение имени
        Product newProduct = productMapper.toEntity(newProductDTO); // Преобразование DTO в сущность
        newProduct.setOnlineCopyPath(fileName); // Установка пути к файлу
        newProduct.setCreatedWhen(LocalDateTime.now()); // Установка даты создания
        newProduct.setCreatedBy(SecurityContextHolder.getContext().getAuthentication().getName()); // Установка создателя

        // Сохраняем новый продукт в базе данных
        Product savedProduct = productRepository.save(newProduct);
        return productMapper.toDTO(savedProduct); // Возвращаем сохраненный продукт как DTO
    }

    public Page<ProductDTO> getAllProducts(Pageable pageable) {
        Page<Product> productsPaginated = productRepository.findAll(pageable);
        List<ProductDTO> result = productMapper.toDTOs(productsPaginated.getContent());
        return new PageImpl<>(result, pageable, productsPaginated.getTotalElements());
    }

    public Page<ProductDTO> searchProducts(String name, String category, Pageable pageRequest) {
        Page<Product> productsPaginated = productRepository.searchProducts(name, category, pageRequest);
        List<ProductDTO> result = productMapper.toDTOs(productsPaginated.getContent());
        return new PageImpl<>(result, pageRequest, productsPaginated.getTotalElements());
    }

    public void deleteSoft(final Long id) throws MyDeleteException {
        Product product = productRepository.findById(id).orElseThrow(
                () -> new NotFoundException("Продукт не найден"));

        // Логика для проверки возможности удаления
        boolean productCanBeDeleted = productRepository.isProductCanBeDeleted(id);

        if (productCanBeDeleted) {
            markAsDeleted(product); // Метод для логического удаления
            productRepository.save(product);
        } else {
            throw new MyDeleteException(Errors.Products.PRODUCT_DELETED_ERROR);
        }
    }

    private void markAsDeleted(Product product) {
        // Логика для пометки продукта как удаленного
        product.setDeleted(true);
        product.setDeletedWhen(LocalDateTime.now()); // Установка даты удаления
    }
}
