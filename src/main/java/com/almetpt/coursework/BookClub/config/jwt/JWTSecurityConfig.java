package com.almetpt.coursework.BookClub.config.jwt;//package com.almetpt.coursework.library.config.jwt;
//
//import com.almetpt.coursework.library.service.userdetails.CustomUserDetailsService;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.authentication.AuthenticationManager;
//import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
//import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.config.http.SessionCreationPolicy;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//import org.springframework.security.web.SecurityFilterChain;
//import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
//import static com.almetpt.coursework.library.constants.SecurityConstants.*;
//import static com.almetpt.coursework.library.constants.UserRoleConstants.ADMIN;
//import static com.almetpt.coursework.library.constants.UserRoleConstants.ORGANIZER;
//
//@Configuration
//@EnableWebSecurity
//@EnableMethodSecurity
//public class JWTSecurityConfig {
//
//    private final CustomUserDetailsService customUserDetailsService;
//    private final JWTTokenFilter jwtTokenFilter;
//
//    public JWTSecurityConfig(BCryptPasswordEncoder bCryptPasswordEncoder,
//                             CustomUserDetailsService customUserDetailsService,
//                             JWTTokenFilter jwtTokenFilter) {
//        this.customUserDetailsService = customUserDetailsService;
//        this.jwtTokenFilter = jwtTokenFilter;
//    }
//
//    @Bean
//    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
//        httpSecurity
//                .cors().disable() // todo протестировать без cors/csrf
//                .csrf().disable()
//                .authorizeHttpRequests((requests) -> requests
//                        .requestMatchers(RESOURCES_WHITE_LIST.toArray(String[]::new)).permitAll()
//                        .requestMatchers(BOOKS_WHITE_LIST.toArray(String[]::new)).permitAll()
//                        .requestMatchers(USERS_WHITE_LIST.toArray(String[]::new)).permitAll()
//                        .requestMatchers(BOOKS_PERMISSION_LIST.toArray(String[]::new)).hasAnyRole(ADMIN, ORGANIZER)
//                        .anyRequest().authenticated() // Все прочие запросы доступны аутентифицированным пользователям
//                )
//                .exceptionHandling()
////                .authenticationEntryPoint()
//                .and()
//                .sessionManagement(
//                        session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
//                )
//                .addFilterBefore(jwtTokenFilter, UsernamePasswordAuthenticationFilter.class)
//                .userDetailsService(customUserDetailsService);
//        return httpSecurity.build();
//    }
//    @Bean
//    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
//            throws Exception {
//        return authenticationConfiguration.getAuthenticationManager();
//    }
//
//}