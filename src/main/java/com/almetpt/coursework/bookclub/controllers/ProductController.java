package com.almetpt.coursework.bookclub.controllers;

import com.almetpt.coursework.bookclub.dto.ProductDTO;
import com.almetpt.coursework.bookclub.dto.ProductImageDTO;
import com.almetpt.coursework.bookclub.dto.ProductImageUploadDTO;
import com.almetpt.coursework.bookclub.model.Product;
import com.almetpt.coursework.bookclub.model.ProductCategory;
import com.almetpt.coursework.bookclub.service.ProductImageService;
import com.almetpt.coursework.bookclub.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/rest/products")
@Tag(name = "Продукты", description = "Контроллер для работы с продуктами")
public class ProductController extends GenericController<Product, ProductDTO> {

    private final ProductService productService;
    private final ProductImageService productImageService;

    public ProductController(ProductService productService, ProductImageService productImageService) {
        super(productService);
        this.productService = productService;
        this.productImageService = productImageService;
    }

    @Override
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ProductDTO>> getAll() {
        return ResponseEntity.ok(productService.listAll());
    }

    @Operation(summary = "Получить страницу продуктов", description = "Возвращает страницу продуктов с пагинацией")
    @GetMapping("/paginated")
    public Page<ProductDTO> getAllProductsPaginated(Pageable pageable) {
        return productService.listAll(pageable);
    }

    @Operation(summary = "Поиск продуктов", description = "Поиск продуктов по названию или категории")
    @GetMapping("/search")
    public Page<ProductDTO> searchProducts(
            @Parameter(description = "Название продукта") @RequestParam(required = false) String name,
            @Parameter(description = "Категория продукта") @RequestParam(required = false) String category,
            Pageable pageable) {
        return productService.searchProducts(name, category, pageable);
    }

    @Operation(summary = "Загрузить изображение обложки продукта", description = "Загружает изображение обложки для указанного продукта в виде файла")
    @PostMapping(path = "/{id}/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductImageDTO> uploadProductCover(
            @Parameter(description = "ID продукта") @PathVariable Long id,
            @Parameter(description = "Файл изображения", content = @io.swagger.v3.oas.annotations.media.Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE, schema = @io.swagger.v3.oas.annotations.media.Schema(type = "string", format = "binary"))) @RequestPart("file") MultipartFile file) {
        try {
            ProductImageDTO result = productImageService.uploadImage(id, file);
            return ResponseEntity.ok(result);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @Operation(summary = "Загрузить изображение обложки продукта из URL", description = "Устанавливает URL внешнего изображения в качестве обложки продукта")
    @PostMapping("/url")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductImageDTO> setProductCoverUrl(
            @Parameter(description = "Информация об изображении") @RequestBody ProductImageUploadDTO dto) {
        ProductImageDTO result = productImageService.uploadImageFromUrl(dto);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Загрузить бинарные данные изображения продукта", description = "Загружает и сохраняет бинарные данные изображения обложки продукта")
    @PostMapping(path = "/{id}/upload-binary", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductImageDTO> uploadProductCoverBinary(
            @Parameter(description = "ID продукта") @PathVariable Long id,
            @Parameter(description = "Файл изображения", content = @io.swagger.v3.oas.annotations.media.Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE, schema = @io.swagger.v3.oas.annotations.media.Schema(type = "string", format = "binary"))) @RequestPart("file") MultipartFile file) {
        try {
            ProductImageDTO result = productImageService.uploadImageData(id, file);
            return ResponseEntity.ok(result);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @Operation(summary = "Получить информацию об изображении продукта", description = "Возвращает информацию об изображении обложки продукта")
    @GetMapping("/{id}/image-info")
    public ResponseEntity<ProductImageDTO> getProductImageInfo(
            @Parameter(description = "ID продукта") @PathVariable Long id) {
        ProductImageDTO result = productImageService.getProductImageInfo(id);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}/cover")
    public ResponseEntity<?> getProductCover(@PathVariable Long id) {
        try {
            // Получаем информацию о продукте
            ProductDTO product = productService.getOne(id);
            // Если есть URL, делаем редирект
            if (product.getCoverImageUrl() != null && !product.getCoverImageUrl().isEmpty()) {
                return ResponseEntity.status(HttpStatus.FOUND)
                        .location(URI.create(product.getCoverImageUrl()))
                        .build();
            }

            // Если есть локальный файл
            if (product.getCoverImageFilename() != null) {
                // Получаем данные файла
                byte[] imageData = productImageService.getProductCoverImageData(id);
                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(determineContentType(product.getCoverImageFilename())))
                        .body(imageData);
            }

            // Если нет ни URL, ни файла
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Определение типа контента по расширению файла
    private String determineContentType(String filename) {
        if (filename.endsWith(".webp"))
            return "image/webp";
        if (filename.endsWith(".jpg") || filename.endsWith(".jpeg"))
            return "image/jpeg";
        if (filename.endsWith(".png"))
            return "image/png";
        return "application/octet-stream";
    }

    @GetMapping("/categories")
    public ResponseEntity<List<String>> getAllProductCategories() {
        return ResponseEntity.ok(
                Arrays.stream(ProductCategory.values())
                        .map(Enum::name)
                        .collect(Collectors.toList()));
    }

    @GetMapping("/categories/with-descriptions")
    public ResponseEntity<List<CategoryInfo>> getAllProductCategoriesWithDescriptions() {
        List<CategoryInfo> categories = Arrays.stream(ProductCategory.values())
                .map(category -> new CategoryInfo(category.name(), category.getDescription()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(categories);
    }

    // Вспомогательный класс для передачи информации о категории
    private static class CategoryInfo {
        private final String name;
        private final String description;

        public CategoryInfo(String name, String description) {
            this.name = name;
            this.description = description;
        }

        @SuppressWarnings("unused")
        public String getName() {
            return name;
        }

        @SuppressWarnings("unused")
        public String getDescription() {
            return description;
        }
    }

}
