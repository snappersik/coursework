package com.almetpt.coursework.bookclub.mapper;

import com.almetpt.coursework.bookclub.dto.ProductDTO;
import com.almetpt.coursework.bookclub.model.Product;
import jakarta.annotation.PostConstruct;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class ProductMapper extends GenericMapper<Product, ProductDTO> {

    public ProductMapper(ModelMapper mapper) {
        super(Product.class, ProductDTO.class, mapper);
    }

    @PostConstruct
    @Override
    protected void setupMapper() {
        modelMapper.createTypeMap(Product.class, ProductDTO.class)
                .addMappings(mapping -> {
                    mapping.map(Product::getCoverImageUrl, ProductDTO::setCoverImageUrl);
                    mapping.map(Product::getCoverImageFilename, ProductDTO::setCoverImageFilename);
                    mapping.map(Product::getOriginalCoverImageFilename, ProductDTO::setOriginalCoverImageFilename);
                    mapping.map(src -> src.hasLocalImage(), ProductDTO::setHasLocalImage);
                    // New mappings for electronic product
                    mapping.map(Product::getElectronicProductFilename, ProductDTO::setElectronicProductFilename);
                    mapping.map(Product::getOriginalElectronicProductFilename, ProductDTO::setOriginalElectronicProductFilename);
                    mapping.map(src -> src.hasElectronicFile(), ProductDTO::setHasElectronicFile);
                })
                .setPostConverter(toDTOConverter());

        modelMapper.createTypeMap(ProductDTO.class, Product.class)
                .addMappings(mapping -> {
                     mapping.map(ProductDTO::getCoverImageUrl, Product::setCoverImageUrl);
                     mapping.map(ProductDTO::getCoverImageFilename, Product::setCoverImageFilename);
                     mapping.map(ProductDTO::getOriginalCoverImageFilename, Product::setOriginalCoverImageFilename);
                     // New mappings for electronic product
                     mapping.map(ProductDTO::getElectronicProductFilename, Product::setElectronicProductFilename);
                     mapping.map(ProductDTO::getOriginalElectronicProductFilename, Product::setOriginalElectronicProductFilename);
                })
                .setPostConverter(toEntityConverter());
    }

    @Override
    protected List<Long> getIds(Product entity) {
        return Collections.emptyList();
    }
}
