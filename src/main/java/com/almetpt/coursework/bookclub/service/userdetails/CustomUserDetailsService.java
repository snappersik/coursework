package com.almetpt.coursework.bookclub.service.userdetails;

import com.almetpt.coursework.bookclub.constants.UserRoleConstants;
import com.almetpt.coursework.bookclub.model.User;
import com.almetpt.coursework.bookclub.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static com.almetpt.coursework.bookclub.constants.UserRoleConstants.ADMIN;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Value("${spring.security.user.name}")
    private String adminEmail;

    @Value("${spring.security.user.password}")
    private String adminPassword;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Используем username как email
        return loadUserByEmail(username);
    }

    public UserDetails loadUserByEmail(String email) throws UsernameNotFoundException {
        if (email.equals(adminEmail)) {
            return new CustomUserDetails(null, email, adminPassword, List.of(new SimpleGrantedAuthority("ROLE_" + ADMIN)));
        } else {
            User user = userRepository.findUserByEmailAndIsDeletedFalse(email);
            if (user == null) {
                throw new UsernameNotFoundException("Пользователь с email: " + email + " не найден");
            }

            List<SimpleGrantedAuthority> authorities = new ArrayList<>();
            authorities.add(new SimpleGrantedAuthority(user.getRole().getId() == 1L
                    ? "ROLE_" + UserRoleConstants.USER
                    : "ROLE_" + UserRoleConstants.ORGANIZER));
            return new CustomUserDetails(user.getId().intValue(), email, user.getPassword(), authorities);
        }
    }
}