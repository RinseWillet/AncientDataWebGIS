package com.webgis.ancientdata.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.List;

@SuppressWarnings("unused")
@EnableMethodSecurity
@Configuration
public class SecurityConfig {

    private static final String SITES_URL = "/api/sites/**";
    private static final String ROAD_URL = "/api/roads/**";
    private static final String ADMIN = "ADMIN";
    private static final String USER = "USER";

    private final JwtFilter jwtFilter;
    private final CustomUserDetailService userDetailService;

    @SuppressWarnings("unused")
    public SecurityConfig(JwtFilter jwtFilter, CustomUserDetailService userDetailService) {
        this.jwtFilter = jwtFilter;
        this.userDetailService = userDetailService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @SuppressWarnings("unused")
    @Bean
    public AuthenticationManager authenticationManager() {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider(userDetailService);
        authenticationProvider.setPasswordEncoder(passwordEncoder());
        return new ProviderManager(List.of(authenticationProvider));
    }

    @SuppressWarnings("unused")
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                .authorizeHttpRequests(auth -> auth
                        // Allow static frontend resources
                        .requestMatchers("/", "/index.html", "/assets/**", "/favicon.ico").permitAll()
                        // API endpoints
                        .requestMatchers(HttpMethod.GET, SITES_URL).permitAll()
                        .requestMatchers(HttpMethod.POST, SITES_URL).hasAnyRole(USER, ADMIN)
                        .requestMatchers(HttpMethod.PUT, SITES_URL).hasAnyRole(USER, ADMIN)
                        .requestMatchers(HttpMethod.DELETE, SITES_URL).hasRole(ADMIN)
                        .requestMatchers(HttpMethod.GET, ROAD_URL).permitAll()
                        .requestMatchers(HttpMethod.POST, ROAD_URL).hasAnyRole(USER, ADMIN) // Only USER/ADMIN can add
                        .requestMatchers(HttpMethod.PUT, ROAD_URL).hasAnyRole(USER, ADMIN) // Only USER/ADMIN can update
                        .requestMatchers(HttpMethod.DELETE, ROAD_URL).hasRole(ADMIN) // Only ADMIN can delete
                        .requestMatchers("/api/auth/register", "/api/auth/login").permitAll()
                        .anyRequest().authenticated()
                )
                .csrf(AbstractHttpConfigurer::disable) // Disable CSRF for JWT
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // Stateless session for JWT
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(HttpStatus.UNAUTHORIZED.value());
                            response.setContentType("application/json");
                            response.getWriter().write("{ \"error\": \"Unauthorized\", \"message\": \"" + authException.getMessage() + "\" }");
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setStatus(HttpStatus.FORBIDDEN.value());
                            response.setContentType("application/json");
                            response.getWriter().write("{ \"error\": \"Forbidden\", \"message\": \"" + accessDeniedException.getMessage() + "\" }");
                        })
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return httpSecurity.build();
    }
}
