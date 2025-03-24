package com.almetpt.coursework.bookclub.repository;

import com.almetpt.coursework.bookclub.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends GenericRepository {
    User findUserByEmail(String email);

    User findUserByEmailAndIsDeletedFalse(String email);

    User findUserByChangePasswordToken(String uuid);

    @Query(nativeQuery = true,
            value = """
           select u.*
           from users u
           where u.first_name ilike '%' || coalesce(:firstName, '%') || '%'
           and u.last_name ilike '%' || coalesce(:lastName, '%') || '%'
           and u.email ilike '%' || coalesce(:email, '%') || '%'
           """)
    Page<User> searchUsers(String firstName, String lastName, String email, Pageable pageable);

    @Query(nativeQuery = true,
            value = """
                select u.email
                from users u
                join orders o on u.id = o.user_id
                where o.status = 'DELIVERED' and o.created_when < now() - interval '14 days'
                  and not exists(
                      select 1
                      from orders o2
                      where o2.user_id = u.id and o2.status = 'DELIVERED' and o2.created_when >= now() - interval '14 days'
                  )
            """)
    List<String> getDelayedEmails();
}
