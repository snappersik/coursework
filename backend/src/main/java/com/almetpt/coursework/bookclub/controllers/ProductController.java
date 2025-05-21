package com.almetpt.coursework.bookclub.controllers;

import com.almetpt.coursework.bookclub.dto.ProductDTO;
// import com.almetpt.coursework.bookclub.dto.ProductImageDTO;
// import com.almetpt.coursework.bookclub.dto.ProductImageUploadDTO;
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

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/rest/products")
@Tag(name = "Продукты", description = "Контроллер для работы с продуктами")
public class ProductController extends GenericController<Product, ProductDTO> {

    private final ProductService productService;
    // private final ProductImageService productImageService;

    public ProductController(ProductService productService, ProductImageService productImageService) {
        super(productService);
        this.productService = productService;
        // this.productImageService = productImageService;
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

    @Operation(description = "Создать продукт с изображением", method = "create")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN') or hasRole('ORGANIZER')")
    public ResponseEntity<ProductDTO> create(
            @RequestPart("product") ProductDTO productDTO,
            @RequestPart(value = "file", required = false) MultipartFile file) {
        try {
            ProductDTO createdProduct = productService.createProductWithImage(productDTO, file);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdProduct);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @Operation(description = "Обновить продукт с изображением", method = "update")
    @PutMapping(value = "/{id}/with-file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN') or hasRole('ORGANIZER')")
    public ResponseEntity<ProductDTO> updateWithFile(
            @PathVariable Long id,
            @RequestPart("product") ProductDTO productDTO,
            @RequestPart(value = "file", required = false) MultipartFile file) {
        try {
            productDTO.setId(id);
            ProductDTO updatedProduct = productService.updateProductWithImage(productDTO, file);
            return ResponseEntity.ok(updatedProduct);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
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
