package com.almetpt.coursework.library.service.userdetails;

import com.aptproject.springlibraryproject.library.constants.UserRoleConstants;
import com.aptproject.springlibraryproject.library.model.User;
import com.aptproject.springlibraryproject.library.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static com.aptproject.springlibraryproject.library.constants.UserRoleConstants.ADMIN;

@Service
public class CustomUserDetailsService
        implements UserDetailsService {

    private final UserRepository userRepository;

    @Value("${spring.security.user.name}") // добавлена строка в application.properties
    private String adminUserName;
    @Value("${spring.security.user.password}") // добавлена строка в application.properties
    private String adminPassword;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        if (username.equals(adminUserName)) {
            return new CustomUserDetails(null, username, adminPassword, List.of(new SimpleGrantedAuthority("ROLE_" +
                    ADMIN)));
        } else {
            User user = userRepository.findUserByLoginAndIsDeletedFalse(username); // создать метод в UserRepository
            List<GrantedAuthority> authorities = new ArrayList<>();

            //ROLE_USER, ROLE_LIBRARIAN
            authorities.add(new SimpleGrantedAuthority(user.getRole().getId() == 1L
                    ? "ROLE_" + UserRoleConstants.USER
                    : "ROLE_" + UserRoleConstants.LIBRARIAN));

            return new CustomUserDetails(user.getId().intValue(), username, user.getPassword(), authorities);
        }
    }
}