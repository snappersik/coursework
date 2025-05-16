package com.almetpt.coursework.bookclub.repository;

import com.almetpt.coursework.bookclub.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends GenericRepository<User> {
    Optional<User> findByEmail(String email); // Добавлен метод

    User findUserByEmailAndIsDeletedFalse(String email);

    User findUserByChangePasswordToken(String uuid);

    @Query(nativeQuery = true, value = """
    SELECT u.*
    FROM users u
    WHERE u.first_name ILIKE '%' || COALESCE(:firstName, '%') || '%'
    AND u.last_name ILIKE '%' || COALESCE(:lastName, '%') || '%'
    AND u.email ILIKE '%' || COALESCE(:email, '%') || '%'
    """)
    Page<User> searchUsers(String firstName, String lastName, String email, Pageable pageable);

    @Query(nativeQuery = true, value = """
    SELECT u.email
    FROM users u
    JOIN orders o ON u.id = o.user_id
    WHERE o.status = 'DELIVERED' AND o.created_when < NOW() - INTERVAL '14 days'
    AND NOT EXISTS(
        SELECT 1
        FROM orders o2
        WHERE o2.user_id = u.id AND o2.status = 'DELIVERED' AND o2.created_when >= NOW() - INTERVAL '14 days'
    )
    """)
    List<String> getDelayedEmails();
}