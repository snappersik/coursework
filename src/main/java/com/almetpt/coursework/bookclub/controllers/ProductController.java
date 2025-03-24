package com.almetpt.coursework.bookclub.controller;

import com.almetpt.coursework.bookclub.controllers.GenericController;
import com.almetpt.coursework.bookclub.dto.ProductDTO;
import com.almetpt.coursework.bookclub.model.Product;
import com.almetpt.coursework.bookclub.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/products")
public class ProductController extends GenericController<Product, ProductDTO> {

    private final ProductService productService;

    @Autowired
    public ProductController(ProductService productService) {
        super(productService);
        this.productService = productService;
    }

    @GetMapping("/all")
    public Page<ProductDTO> getAllProducts(Pageable pageable) {
        return productService.getAllProducts(pageable);
    }

    @GetMapping("/search")
    public Page<ProductDTO> searchProducts(@RequestParam(required = false) String name,
                                           @RequestParam(required = false) String category,
                                           Pageable pageable) {
        return productService.searchProducts(name, category, pageable);
    }
}
