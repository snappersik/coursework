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
                .setPostConverter(toDTOConverter());
        modelMapper.createTypeMap(ProductDTO.class, Product.class)
                .setPostConverter(toEntityConverter());
    }

    @Override
    protected List<Long> getIds(Product entity) {
        return Collections.emptyList();
    }
}
