package com.almetpt.coursework.bookclub.service;

import com.almetpt.coursework.bookclub.dto.ProductDTO;
import com.almetpt.coursework.bookclub.mapper.ProductMapper;
import com.almetpt.coursework.bookclub.model.Product;
import com.almetpt.coursework.bookclub.model.ProductCategory;
import com.almetpt.coursework.bookclub.repository.ProductRepository;
import jakarta.transaction.Transactional;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.webjars.NotFoundException;

import java.io.IOException;
import java.util.List;

@Service
public class ProductService extends GenericService<Product, ProductDTO> {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    // private final ProductImageService productImageService;
    private final FileStorageService fileStorageService; // For direct file operations

    public ProductService(ProductRepository productRepository,
                          ProductMapper productMapper,
                          ProductImageService productImageService,
                          FileStorageService fileStorageService) {
        super(productRepository, productMapper);
        this.productRepository = productRepository;
        this.productMapper = productMapper;
        // this.productImageService = productImageService;
        this.fileStorageService = fileStorageService;
    }

    public Page<ProductDTO> searchProducts(String name, String categoryName, Pageable pageable) {
        ProductCategory category = null;
        if (categoryName != null && !categoryName.isEmpty() && !"all".equalsIgnoreCase(categoryName)) {
            try {
                category = ProductCategory.valueOf(categoryName.toUpperCase());
            } catch (IllegalArgumentException ignored) {
            }
        }
        if (name == null) name = "";
        Page<Product> page = productRepository.findAllByNameAndCategory(name, category, pageable);
        List<ProductDTO> dtos = productMapper.toDTOs(page.getContent());
        return new PageImpl<>(dtos, pageable, page.getTotalElements());
    }

    @Transactional
    public ProductDTO createProductWithFiles(ProductDTO productDTO, MultipartFile coverFile, MultipartFile electronicFile) throws IOException {
        Product product = productMapper.toEntity(productDTO);

        if (coverFile != null && !coverFile.isEmpty()) {
            String coverFilename = fileStorageService.saveFile(coverFile);
            product.setCoverImageFilename(coverFilename);
            product.setOriginalCoverImageFilename(coverFile.getOriginalFilename());
            product.setCoverImageUrl(null); // Clear URL if file is uploaded
            product.setCoverImageData(null); // Clear blob data if file is uploaded
        } else if (productDTO.getCoverImageUrl() != null && !productDTO.getCoverImageUrl().isEmpty()) {
            product.setCoverImageUrl(productDTO.getCoverImageUrl());
            product.setCoverImageFilename(null);
            product.setCoverImageData(null);
        }


        if (electronicFile != null && !electronicFile.isEmpty()) {
            String electronicFilename = fileStorageService.saveFile(electronicFile);
            product.setElectronicProductFilename(electronicFilename);
            product.setOriginalElectronicProductFilename(electronicFile.getOriginalFilename());
        }

        Product savedProduct = productRepository.save(product);
        return productMapper.toDTO(savedProduct);
    }

    @Transactional
    public ProductDTO updateProductWithFiles(Long productId, ProductDTO productDTO, MultipartFile coverFile, MultipartFile electronicFile) throws IOException {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("Product not found with id: " + productId));

        // Update basic fields from DTO
        product.setName(productDTO.getName());
        product.setDescription(productDTO.getDescription());
        product.setPrice(productDTO.getPrice());
        product.setCategory(productDTO.getCategory());
        // isDeleted and audit fields are handled by GenericModel/Service

        if (coverFile != null && !coverFile.isEmpty()) {
            // Delete old cover file if exists
            if (product.getCoverImageFilename() != null) {
                fileStorageService.deleteFile(product.getCoverImageFilename());
            }
            String coverFilename = fileStorageService.saveFile(coverFile);
            product.setCoverImageFilename(coverFilename);
            product.setOriginalCoverImageFilename(coverFile.getOriginalFilename());
            product.setCoverImageUrl(null);
            product.setCoverImageData(null);
        } else if (productDTO.getCoverImageUrl() != null && !productDTO.getCoverImageUrl().isEmpty()) {
            // If URL is provided and no new file, update URL and clear local file
            if (product.getCoverImageFilename() != null) {
                fileStorageService.deleteFile(product.getCoverImageFilename());
                product.setCoverImageFilename(null);
                product.setOriginalCoverImageFilename(null);
            }
            product.setCoverImageUrl(productDTO.getCoverImageUrl());
            product.setCoverImageData(null);
        }
        // If coverUrl is explicitly set to empty and no file is uploaded, clear existing cover
        else if ((productDTO.getCoverImageUrl() == null || productDTO.getCoverImageUrl().isEmpty()) && coverFile == null) {
             if (product.getCoverImageFilename() != null) {
                fileStorageService.deleteFile(product.getCoverImageFilename());
                product.setCoverImageFilename(null);
                product.setOriginalCoverImageFilename(null);
            }
            product.setCoverImageUrl(null);
            product.setCoverImageData(null);
        }


        if (electronicFile != null && !electronicFile.isEmpty()) {
            // Delete old electronic file if exists
            if (product.getElectronicProductFilename() != null) {
                fileStorageService.deleteFile(product.getElectronicProductFilename());
            }
            String electronicFilename = fileStorageService.saveFile(electronicFile);
            product.setElectronicProductFilename(electronicFilename);
            product.setOriginalElectronicProductFilename(electronicFile.getOriginalFilename());
        }

        Product updatedProduct = productRepository.save(product);
        return productMapper.toDTO(updatedProduct);
    }
        
    public Resource getElectronicProductResource(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("Product not found with id: " + productId));
        if (product.getElectronicProductFilename() == null) {
            throw new NotFoundException("Electronic file not found for product id: " + productId);
        }
        return fileStorageService.loadAsResource(product.getElectronicProductFilename());
    }
        
    public Product getProductForDownload(Long productId) { // Helper method to get product details
         return productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("Product not found with id: " + productId));
    }


    public boolean canDeleteProduct(Long productId) {
        return true;
    }
}
