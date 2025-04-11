package com.almetpt.coursework.bookclub.controllers;

import com.almetpt.coursework.bookclub.dto.GenericDTO;
import com.almetpt.coursework.bookclub.exception.MyDeleteException;
import com.almetpt.coursework.bookclub.model.GenericModel;
import com.almetpt.coursework.bookclub.service.GenericService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
public abstract class GenericController<E extends GenericModel, D extends GenericDTO> {

    protected GenericService<E, D> service;

    protected GenericController(GenericService<E, D> genericService) {
        this.service = genericService;
    }

    @Operation(description = "Получить запись по Id", method = "getById")
    @GetMapping("/{id}")
    public ResponseEntity<D> getById(@PathVariable(value = "id") Long id) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(service.getOne(id));
    }

    @Operation(description = "Получить все записи (включая удаленные)", method = "getAll")
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')") // Только для администраторов
    public ResponseEntity<List<D>> getAll() {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(service.listAll());
    }

    @Operation(description = "Получить все неудаленные записи", method = "getAllActive")
    @GetMapping
    public ResponseEntity<List<D>> getAllActive() {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(service.listAllNotDeleted());
    }

    @Operation(description = "Создать запись", method = "create")
    @PostMapping
    public ResponseEntity<D> create(@RequestBody D newEntity) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(newEntity));
    }

    @Operation(description = "Обновить запись", method = "update")
    @PutMapping("/{id}")
    public ResponseEntity<D> update(@RequestBody D updateEntity,
            @PathVariable(value = "id") Long id) {
        updateEntity.setId(id);
        return ResponseEntity.status(HttpStatus.OK).body(service.update(updateEntity));
    }

    @Operation(description = "Частично обновить запись", method = "partialUpdate")
    @PatchMapping("/{id}")
    public ResponseEntity<D> partialUpdate(@RequestBody D patchEntity,
            @PathVariable(value = "id") Long id) {
        patchEntity.setId(id);
        return ResponseEntity.status(HttpStatus.OK).body(service.partialUpdate(patchEntity));
    }

    @Operation(description = "Удалить запись", method = "delete")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable(value = "id") Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(description = "Мягкое удаление записи", method = "softDelete")
    @DeleteMapping("/soft/{id}")
    public ResponseEntity<Void> softDelete(@PathVariable(value = "id") Long id) throws MyDeleteException {
        service.deleteSoft(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(description = "Восстановить удаленную запись", method = "restore")
    @PatchMapping("/restore/{id}")
    public ResponseEntity<Void> restore(@PathVariable(value = "id") Long id) {
        service.restore(id);
        return ResponseEntity.noContent().build();
    }

}
