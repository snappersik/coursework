package com.almetpt.coursework.bookclub.mapper;

import com.almetpt.coursework.bookclub.dto.GenericDTO;
import com.almetpt.coursework.bookclub.model.GenericModel;

import java.util.List;

public interface Mapper<E extends GenericModel, D extends GenericDTO> {
    E toEntity(D dto);
    D toDTO(E entity);
    List<E> toEntities(List<D> dtos);
    List<D> toDTOs(List<E> entities);
}
