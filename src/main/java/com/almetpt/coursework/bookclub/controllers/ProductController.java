package com.almetpt.coursework.bookclub.controllers;

import com.almetpt.coursework.bookclub.dto.ProductDTO;
import com.almetpt.coursework.bookclub.model.Product;
import com.almetpt.coursework.bookclub.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rest/products")
@Tag(name = "Продукты", description = "Контроллер для работы с продуктами")
public class ProductController extends GenericController<Product, ProductDTO> {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        super(productService);
        this.productService = productService;
    }

    @Override
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ProductDTO>> getAll() {
        return ResponseEntity.ok(productService.listAll());
    }

    @Operation(
        summary = "Получить страницу продуктов",
        description = "Возвращает страницу продуктов с пагинацией"
    )
    @GetMapping("/paginated")
    public Page<ProductDTO> getAllProductsPaginated(Pageable pageable) {
        return productService.listAll(pageable);
    }

    @Operation(
        summary = "Поиск продуктов",
        description = "Поиск продуктов по названию или категории"
    )
    @GetMapping("/search")
    public Page<ProductDTO> searchProducts(
            @Parameter(description = "Название продукта") @RequestParam(required = false) String name,
            @Parameter(description = "Категория продукта") @RequestParam(required = false) String category,
            Pageable pageable) {
        return productService.searchProducts(name, category, pageable);
    }
}
