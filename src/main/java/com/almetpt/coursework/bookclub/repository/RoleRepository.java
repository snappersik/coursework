package com.almetpt.coursework.bookclub.repository;

import com.almetpt.coursework.bookclub.model.Role;

import java.util.Optional;

public interface RoleRepository extends GenericRepository<Role> {
    Optional<Role> findByName(String name);
}
