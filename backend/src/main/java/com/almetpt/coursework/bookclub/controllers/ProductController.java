package com.almetpt.coursework.bookclub.controllers;

import com.almetpt.coursework.bookclub.dto.ProductDTO;
import com.almetpt.coursework.bookclub.model.Product;
import com.almetpt.coursework.bookclub.model.ProductCategory;
import com.almetpt.coursework.bookclub.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.webjars.NotFoundException;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/rest/products")
@Tag(name = "Продукты", description = "Контроллер для работы с продуктами")
public class ProductController extends GenericController<Product, ProductDTO> {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        super(productService); // Pass productService to GenericController
        this.productService = productService;
    }

    @Operation(summary = "Создать новый продукт с файлами", description = "Создает продукт с возможностью загрузки обложки и электронного файла")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductDTO> createProductWithFiles(
            @Parameter(description = "Данные продукта в формате JSON", required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProductDTO.class))) @RequestPart("product") ProductDTO productDTO,
            @Parameter(description = "Файл обложки продукта (необязательно)", content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE)) @RequestPart(name = "coverFile", required = false) MultipartFile coverFile,
            @Parameter(description = "Электронный файл продукта (PDF, MP3, ZIP; необязательно)", content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE)) @RequestPart(name = "electronicFile", required = false) MultipartFile electronicFile) {
        try {
            ProductDTO createdProduct = productService.createProductWithFiles(productDTO, coverFile, electronicFile);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdProduct);
        } catch (IOException e) {
            // Log error e
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Обновить существующий продукт с файлами", description = "Обновляет продукт с возможностью загрузки новой обложки и/или электронного файла")
    @PutMapping(path = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductDTO> updateProductWithFiles(
            @Parameter(description = "ID продукта для обновления", required = true) @PathVariable Long id,
            @Parameter(description = "Данные продукта в формате JSON", required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProductDTO.class))) @RequestPart("product") ProductDTO productDTO,
            @Parameter(description = "Новый файл обложки продукта (необязательно)", content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE)) @RequestPart(name = "coverFile", required = false) MultipartFile coverFile,
            @Parameter(description = "Новый электронный файл продукта (PDF, MP3, ZIP; необязательно)", content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE)) @RequestPart(name = "electronicFile", required = false) MultipartFile electronicFile) {
        try {
            productDTO.setId(id); // Ensure DTO has the ID for update context
            ProductDTO updatedProduct = productService.updateProductWithFiles(id, productDTO, coverFile,
                    electronicFile);
            return ResponseEntity.ok(updatedProduct);
        } catch (IOException e) {
            // Log error e
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (NotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Скачать электронный файл продукта", description = "Позволяет скачать прикрепленный электронный файл для продукта")
    @GetMapping("/{id}/download-electronic")
    @PreAuthorize("isAuthenticated()") // Consider more specific authorization (e.g., user purchased this)
    public ResponseEntity<Resource> downloadElectronicProduct(
            @Parameter(description = "ID продукта", required = true) @PathVariable Long id) {
        try {
            Product product = productService.getProductForDownload(id); // Get product to access original filename
            if (product.getElectronicProductFilename() == null
                    || product.getOriginalElectronicProductFilename() == null) {
                return ResponseEntity.notFound().build();
            }
            Resource resource = productService.getElectronicProductResource(id);
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=\"" + product.getOriginalElectronicProductFilename() + "\"");
            // You might want to determine content type more dynamically if needed
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            return ResponseEntity.ok().headers(headers).body(resource);
        } catch (NotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            // Log error e
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/categories/all-with-descriptions") // Changed the mapping to make it unique
    public ResponseEntity<List<Map<String, String>>> getAllCategoriesWithDescriptions() {
        List<Map<String, String>> categoryInfos = Arrays.stream(ProductCategory.values())
                .map(category -> {
                    Map<String, String> catMap = new HashMap<>();
                    catMap.put("name", category.name());
                    catMap.put("description", category.getDescription());
                    return catMap;
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(categoryInfos);
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
    @PostMapping(value = "/create-with-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN') or hasRole('ORGANIZER')")
    public ResponseEntity<ProductDTO> create(
            @RequestPart("product") ProductDTO productDTO,
            @RequestPart(value = "file", required = false) MultipartFile file) {
        try {
            // Заменяем вызов на существующий метод
            ProductDTO createdProduct = productService.createProductWithFiles(productDTO, file, null);
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
            // Заменяем вызов на существующий метод
            ProductDTO updatedProduct = productService.updateProductWithFiles(id, productDTO, file, null);
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
