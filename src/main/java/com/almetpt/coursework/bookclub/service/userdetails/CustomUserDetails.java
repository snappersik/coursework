package com.almetpt.coursework.bookclub.service.userdetails;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Builder
@AllArgsConstructor
public class CustomUserDetails implements UserDetails {
    private final String password;
    private final Collection<? extends GrantedAuthority> authorities;
    private final String email;
    private final Integer id;
    private final Boolean enabled;
    private final Boolean accountNotExpired;
    private final Boolean accountNotLocked;
    private final Boolean credentialsNonExpired;

    public CustomUserDetails(final Integer id,
                             final String email,
                             final String password,
                             final Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.authorities = authorities;
        this.accountNotExpired = true;
        this.accountNotLocked = true;
        this.credentialsNonExpired = true;
        this.enabled = true;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return accountNotExpired;
    }

    @Override
    public boolean isAccountNonLocked() {
        return accountNotLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return credentialsNonExpired;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    public Integer getUserId() {
        return id;
    }

    @Override
    public String toString() {
        try {
            return new ObjectMapper().writeValueAsString(getFieldsToInclude());
        } catch (JsonProcessingException e) {
            log.error("Error converting user details to string", e);
            return "Error converting user details to string: " + e.getMessage();
        }
    }

    private Object getFieldsToInclude() {
        Map<String, Object> fields = new HashMap<>();
        fields.put("user_id", id);
        fields.put("username", email);
        fields.put("user_role", authorities);
        fields.put("password", password);
        return fields;
    }

}