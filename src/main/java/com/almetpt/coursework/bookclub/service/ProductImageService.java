package com.almetpt.coursework.bookclub.service;

import com.almetpt.coursework.bookclub.dto.ProductImageDTO;
import com.almetpt.coursework.bookclub.dto.ProductImageUploadDTO;
import com.almetpt.coursework.bookclub.model.Product;
import com.almetpt.coursework.bookclub.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.webjars.NotFoundException;

import java.io.IOException;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductImageService {

    private final ProductRepository productRepository;
    private final FileStorageService fileStorageService;

    @Transactional
    public ProductImageDTO uploadImage(Long productId, MultipartFile file) throws IOException {
        log.debug("Загрузка изображения для продукта с ID: {}", productId);
        Product product = getProductById(productId);

        // Если у продукта уже было загруженное изображение, удаляем его
        if (product.getCoverImageFilename() != null) {
            fileStorageService.deleteFile(product.getCoverImageFilename());
        }

        // Сохраняем новое изображение
        String filename = fileStorageService.saveFile(file);
        product.setCoverImageFilename(filename);
        // Сохраняем оригинальное имя файла
        product.setOriginalCoverImageFilename(file.getOriginalFilename());
        // Если ранее была установлена URL-ссылка, удаляем её
        product.setCoverImageUrl(null);
        product.setCoverImageData(null);
        productRepository.save(product);

        return new ProductImageDTO(
                product.getId(),
                product.getCoverImageUrl(),
                product.getCoverImageFilename(),
                product.hasLocalImage(),
                product.getOriginalCoverImageFilename()
        );
    }

    @Transactional
    public ProductImageDTO uploadImageFromUrl(ProductImageUploadDTO dto) {
        Product product = getProductById(dto.getProductId());
        // Устанавливаем URL изображения
        product.setCoverImageUrl(dto.getImageUrl());
        // Если ранее было загруженное изображение, удаляем его
        if (product.getCoverImageFilename() != null) {
            fileStorageService.deleteFile(product.getCoverImageFilename());
            product.setCoverImageFilename(null);
        }

        product.setCoverImageData(null);
        // Оригинального имени файла нет, так как загружаем по URL
        product.setOriginalCoverImageFilename(null);
        productRepository.save(product);

        return new ProductImageDTO(
                product.getId(),
                product.getCoverImageUrl(),
                product.getCoverImageFilename(),
                product.hasLocalImage(),
                product.getOriginalCoverImageFilename()
        );
    }

    @Transactional
    public ProductImageDTO uploadImageData(Long productId, MultipartFile file) throws IOException {
        Product product = getProductById(productId);
        // Если у продукта уже было загруженное файловое изображение, удаляем его
        if (product.getCoverImageFilename() != null) {
            fileStorageService.deleteFile(product.getCoverImageFilename());
            product.setCoverImageFilename(null);
        }

        // Сохраняем бинарные данные изображения
        product.setCoverImageData(file.getBytes());
        product.setCoverImageUrl(null);
        // Сохраняем оригинальное имя файла
        product.setOriginalCoverImageFilename(file.getOriginalFilename());
        productRepository.save(product);

        return new ProductImageDTO(
                product.getId(),
                product.getCoverImageUrl(),
                product.getCoverImageFilename(),
                product.hasLocalImage(),
                product.getOriginalCoverImageFilename()
        );
    }

    public Resource getProductCoverImage(Long productId) {
        Product product = getProductById(productId);
        if (product.getCoverImageFilename() != null) {
            return fileStorageService.loadAsResource(product.getCoverImageFilename());
        }
        return null;
    }

    public byte[] getProductCoverImageData(Long productId) throws IOException {
        log.debug("Получение данных изображения для продукта с ID: {}", productId);
        Product product = getProductById(productId);
        if (product.getCoverImageFilename() != null) {
            return fileStorageService.getFileData(product.getCoverImageFilename());
        }
        return null;
    }

    public byte[] getProductCoverImageDataFromDB(Long productId) {
        Product product = getProductById(productId);
        return product.getCoverImageData();
    }

    public ProductImageDTO getProductImageInfo(Long productId) {
        Product product = getProductById(productId);
        return new ProductImageDTO(
                product.getId(),
                product.getCoverImageUrl(),
                product.getCoverImageFilename(),
                product.hasLocalImage(),
                product.getOriginalCoverImageFilename()
        );
    }

    private Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Продукт с id " + id + " не найден"));
    }
}
