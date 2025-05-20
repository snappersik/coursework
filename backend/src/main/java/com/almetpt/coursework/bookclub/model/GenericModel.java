package com.almetpt.coursework.bookclub.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@MappedSuperclass

public abstract class GenericModel {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "default_generator")
    private Long id;

    @Column(name = "created_by")
    private String createdBy;
    
    @Column(name = "created_when")
    private LocalDateTime createdWhen;
    
    @Column(name = "updated_by")
    private String updatedBy;
    
    @Column(name = "updated_when")
    private LocalDateTime updatedWhen;
    
    @Column(name = "deleted_when")
    private LocalDateTime deletedWhen;

    @Column(name = "deleted_by") 
    private String deletedBy;

    @Column(name = "is_deleted", columnDefinition = "boolean default false")
    private boolean isDeleted;
}