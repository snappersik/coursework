package com.almetpt.coursework.bookclub.mapper;

import com.almetpt.coursework.bookclub.dto.GenericDTO;

import com.almetpt.coursework.bookclub.model.GenericModel;

import jakarta.annotation.PostConstruct;

import org.modelmapper.Converter;

import org.modelmapper.ModelMapper;

import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

import java.util.Objects;

@Component
public abstract class GenericMapper<E extends GenericModel,
        D extends GenericDTO> implements Mapper<E, D> {

     private final Class<E> entityClass;

     private final Class<D> dtoClass;

     protected final ModelMapper modelMapper;

     public GenericMapper(Class<E> entityClass, Class<D> dtoClass, ModelMapper modelMapper) {
          this.entityClass = entityClass;
          this.dtoClass = dtoClass;
          this.modelMapper = modelMapper;
     }

     @Override
     public D toDTO(E entity) {
          return Objects.isNull(entity) ? null : modelMapper.map(entity, dtoClass);
     }

     @Override
     public E toEntity(D dto) {
          return Objects.isNull(dto) ? null : modelMapper.map(dto, entityClass);
     }

     @Override
     public List<D> toDTOs(List<E> entities) {
          return entities.stream().map(this::toDTO).toList();
     }

     @Override
     public List<E> toEntities(List<D> dtos) {
          return dtos.stream().map(this::toEntity).toList();
     }

     protected Converter<E, D> toDTOConverter() {
          return context -> {
               E source = context.getSource();
               D destination = context.getDestination();
               mapSpecificFields(source, destination);
               return context.getDestination();
          };
     }

     protected Converter<D, E> toEntityConverter() {
          return context -> {
               D source = context.getSource();
               E destination = context.getDestination();
               mapSpecificFields(source, destination);
               return context.getDestination();
          };
     }

     protected void mapSpecificFields(D source, E destination) {
     }

     protected void mapSpecificFields(E source, D destination) {
     }

     @PostConstruct
     protected void setupMapper() {
     }

     protected List<Long> getIds(E entity) {
          return Collections.emptyList();
     }
}
