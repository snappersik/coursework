package com.almetpt.coursework.BookClub.mapper;

import com.almetpt.coursework.BookClub.dto.GenericDTO;
import com.almetpt.coursework.BookClub.model.GenericModel;

import java.util.List;

public interface Mapper<E extends GenericModel, D extends GenericDTO> {

    E toEntity(D dto);
    D toDTO(E entity); //
    List<E> toEntities(List<D> dtos);
    List<D> toDTOs(List<E> entities);
}
