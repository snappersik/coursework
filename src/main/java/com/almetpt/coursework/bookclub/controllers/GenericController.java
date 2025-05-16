package com.almetpt.coursework.bookclub.controllers;

import com.almetpt.coursework.bookclub.annotations.AdminAction;
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

    protected final GenericService<E, D> service;

    protected GenericController(GenericService<E, D> genericService) {
        this.service = genericService;
    }

    @Operation(description = "Получить запись по Id", method = "getById")
    @GetMapping("/{id}")
    public ResponseEntity<D> getById(@PathVariable Long id) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(service.getOne(id));
    }

    @Operation(description = "Получить все записи (включая удаленные)", method = "getAll")
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
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
    @PreAuthorize("hasRole('ADMIN') or hasRole('ORGANIZER')")
    public ResponseEntity<D> create(@RequestBody D newEntity) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(service.create(newEntity));
    }

    @Operation(description = "Обновить запись", method = "update")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ORGANIZER')")
    public ResponseEntity<D> update(@RequestBody D updateEntity,
                                    @PathVariable Long id) {
        updateEntity.setId(id);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(service.update(updateEntity));
    }

    @Operation(description = "Жёсткое удаление записи", method = "delete")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ORGANIZER')")
    @AdminAction
    public ResponseEntity<Void> delete(@PathVariable Long id) throws MyDeleteException {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(description = "Мягкое удаление записи", method = "softDelete")
    @DeleteMapping("/soft/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @AdminAction
    public ResponseEntity<Void> softDelete(@PathVariable Long id) throws MyDeleteException {
        service.deleteSoft(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(description = "Восстановить удаленную запись", method = "restore")
    @PatchMapping("/restore/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @AdminAction
    public ResponseEntity<Void> restore(@PathVariable Long id) throws MyDeleteException {
        service.restore(id);
        return ResponseEntity.noContent().build();
    }
}
