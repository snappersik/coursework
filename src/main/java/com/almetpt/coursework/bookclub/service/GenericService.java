package com.almetpt.coursework.bookclub.service;

import com.almetpt.coursework.bookclub.dto.GenericDTO;
import com.almetpt.coursework.bookclub.exception.MyDeleteException;
import com.almetpt.coursework.bookclub.mapper.GenericMapper;
import com.almetpt.coursework.bookclub.model.GenericModel;
import com.almetpt.coursework.bookclub.repository.GenericRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.security.core.Authentication;
import org.webjars.NotFoundException;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Абстрактный сервис который хранит в себе реализацию CRUD операций по умолчанию
 * Если реализация отличная от того что представлено в этом классе,
 * то она переопределяется в сервисе для конкретной сущности
 *
 * @param <E> - Сущность с которой мы работаем
 * @param <D> - DTO, которую мы будем отдавать/принимать дальше
 */
@Service
public abstract class GenericService<E extends GenericModel, D extends GenericDTO> {

    protected final GenericRepository<E> repository;
    protected final GenericMapper<E, D> mapper;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public GenericService(GenericRepository<E> repository, GenericMapper<E, D> mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public List<D> listAll() {
        return mapper.toDTOs(repository.findAll());
    }

    public Page<D> listAll(Pageable pageable) {
        Page<E> objects = repository.findAll(pageable);
        List<D> result = mapper.toDTOs(objects.getContent());
        return new PageImpl<>(result, pageable, objects.getTotalElements());
    }

    // SOFT DELETE
    public Page<D> listAllNotDeleted(Pageable pageable) {
        Page<E> preResults = repository.findAllByIsDeletedFalse(pageable);
        List<D> result = mapper.toDTOs(preResults.getContent());
        return new PageImpl<>(result, pageable, preResults.getTotalElements());
    }

    public List<D> listAllNotDeleted() {
        return mapper.toDTOs(repository.findAllByIsDeletedFalse());
    }
    
    public D getOne(final Long id) {
        E entity = repository.findById(id)
            .orElseThrow(() -> new NotFoundException("Данные по заданному id:" + id + " не найдены!"));
        
        // Проверяем роль пользователя
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = authentication.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        
        // Если запись удалена и пользователь не админ - выбрасываем исключение
        if (entity.isDeleted() && !isAdmin) {
            throw new NotFoundException("Данные по заданному id:" + id + " не найдены!");
        }
        
        return mapper.toDTO(entity);
    }
    

    public D create(D newObject) {
        E entity = mapper.toEntity(newObject);
        setAuditFields(entity, true);
        return mapper.toDTO(repository.save(entity));
    }

    public D update(D updatedObject) {
        E entity = mapper.toEntity(updatedObject);
        setAuditFields(entity, false);
        return mapper.toDTO(repository.save(entity));
    }

    protected void setAuditFields(GenericModel entity, boolean isNew) {
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        LocalDateTime now = LocalDateTime.now();
        if (isNew) {
            entity.setCreatedBy(currentUsername);
            entity.setCreatedWhen(now);
        } else {
            entity.setUpdatedBy(currentUsername);
            entity.setUpdatedWhen(now);
        }
    }

    public D partialUpdate(D patchObject) {
        Long id = patchObject.getId();
        E entity = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Данные по заданному id:" + id + " не найдены!"));
        
        E patchEntity = mapper.toEntity(patchObject);
        // Применяем только непустые поля из patchEntity к существующей entity
        // Используем рефлексию для обхода всех полей
        for (java.lang.reflect.Field field : patchEntity.getClass().getDeclaredFields()) {
            try {
                field.setAccessible(true);
                Object value = field.get(patchEntity);
                // Обновляем только если значение не null
                if (value != null) {
                    field.set(entity, value);
                }
            } catch (IllegalAccessException e) {
                // Обработка исключения
            }
        }
        
        entity.setUpdatedWhen(LocalDateTime.now());
        entity.setUpdatedBy(SecurityContextHolder.getContext().getAuthentication().getName());
        return mapper.toDTO(repository.save(entity));
    }

    public void delete(final Long id) {
        repository.deleteById(id);
    }

    // SOFT DELETE
    public void deleteSoft(final Long id) throws MyDeleteException {
        E obj = repository.findById(id).orElseThrow(() -> new NotFoundException("Объект не найден"));
        markAsDeleted(obj);
        repository.save(obj);
    }

    public void restore(final Long id) {
        E obj = repository.findById(id).orElseThrow(() -> new NotFoundException("Объект не найден"));
        unMarkAsDeleted(obj);
        repository.save(obj);
    }

    public void markAsDeleted(GenericModel genericModel) {
        genericModel.setDeleted(true);
        genericModel.setDeletedWhen(LocalDateTime.now());
        genericModel.setDeletedBy(SecurityContextHolder.getContext().getAuthentication().getName());
    }

    public void unMarkAsDeleted(GenericModel genericModel) {
        genericModel.setDeleted(false);
        genericModel.setDeletedWhen(null);
        genericModel.setDeletedBy(null);
    }
}