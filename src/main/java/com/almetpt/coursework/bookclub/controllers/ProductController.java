package com.almetpt.coursework.bookclub.controllers;

import com.almetpt.coursework.bookclub.dto.ProductDTO;
import com.almetpt.coursework.bookclub.model.Product;
import com.almetpt.coursework.bookclub.service.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/products")
public class ProductController extends GenericController<Product, ProductDTO> {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        super(productService);
        this.productService = productService;
    }

    @Override
    @GetMapping("/all")
    public ResponseEntity<List<ProductDTO>> getAll() {
        return ResponseEntity.ok(productService.listAll());
    }    

    @GetMapping("/paginated")
    public Page<ProductDTO> getAllProductsPaginated(Pageable pageable) {
        return productService.listAll(pageable);
    }

    @GetMapping("/search")
    public Page<ProductDTO> searchProducts(@RequestParam(required = false) String name,
            @RequestParam(required = false) String category,
            Pageable pageable) {
        return productService.searchProducts(name, category, pageable);
    }
}
