package com.almetpt.coursework.bookclub.repository;

import com.almetpt.coursework.bookclub.model.GenericModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.List;

/**
 * Абстрактный репозиторий
 * Необходим для работы абстрактного сервиса
 * т.к. в абстрактном сервисе мы не можем использовать конкретный репозиторий,
 * а должны указывать параметризованный (GenericRepository)
 * @param <E> - Сущность, с которой работает репозиторий
 */

@NoRepositoryBean // не даст создать репозиторий, т.к. он абстрактный. Аналог @MappedSuperclass у GenericModel
public interface GenericRepository <T extends GenericModel> extends JpaRepository<T, Long> { // Ограничиваем работу

    Page<T> findAllByIsDeletedFalse(Pageable pageable);
    List<T> findAllByIsDeletedFalse();

}