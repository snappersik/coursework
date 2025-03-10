package com.almetpt.coursework.library.mapper;

import com.aptproject.springlibraryproject.library.dto.GenericDTO;
import com.aptproject.springlibraryproject.library.model.GenericModel;
import jakarta.annotation.PostConstruct;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component
public abstract class GenericMapper<E extends GenericModel, D extends GenericDTO>
        implements Mapper<E, D> {

     // Внедрение всех необходимых зависимостей
     private final Class<E> entityClass;
     private final Class<D> dtoClass;
     protected final ModelMapper modelMapper;

     public GenericMapper(Class<E> entityClass,
                          Class<D> dtoClass,
                          ModelMapper modelMapper) {
          this.entityClass = entityClass;
          this.dtoClass = dtoClass;
          this.modelMapper = modelMapper;
     }

     @Override
     public E toEntity(D dto) {
          return Objects.isNull(dto)
                  ? null
                  : modelMapper.map(dto, entityClass);
     }

     @Override
     public D toDTO(E entity) {
          return Objects.isNull(entity)
                  ? null
                  : modelMapper.map(entity, dtoClass);
     }

     @Override
     public List<E> toEntities(List<D> dtos) {
          return dtos.stream().map(this::toEntity).toList();
     }

     @Override
     public List<D> toDTOs(List<E> entities) {
          return entities.stream().map(this::toDTO).toList();
     }

     protected Converter<D, E> toEntityConverter() {
          return context -> {
               D source = context.getSource();
               E destination = context.getDestination();
               mapSpecificFields(source, destination);
               return context.getDestination();
          };
     }

     protected Converter<E, D> toDTOConverter() {
          return context -> {
               E source = context.getSource();
               D destination = context.getDestination();
               mapSpecificFields(source, destination);
               return context.getDestination();
          };
     }

     protected void mapSpecificFields(D source, E destination) {
     }
     protected void mapSpecificFields(E source, D destination) {
     }

     /**
      * Настройка маппера на случай несовпадения source/destination
      */

     @PostConstruct
     protected  abstract void setupMapper();

     protected abstract List<Long> getIds(E Entity);

}
