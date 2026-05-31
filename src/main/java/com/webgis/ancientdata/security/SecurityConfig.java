package com.webgis.ancientdata.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
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
    private static final String DASHBOARD_URL = "/api/dashboard/**";
    private static final String MEDIA_URL = "/api/media/**";
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
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(auth -> auth
                        // Allow static frontend resources
                        .requestMatchers("/", "/index.html", "/assets/**", "/favicon.ico").permitAll()
                        // API endpoints
                        .requestMatchers(HttpMethod.GET, SITES_URL).permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/sites/*/modern-reference").hasAnyRole(USER, ADMIN)
                        .requestMatchers(HttpMethod.POST, SITES_URL).denyAll()
                        .requestMatchers(HttpMethod.PUT, SITES_URL).denyAll()
                        .requestMatchers(HttpMethod.DELETE, SITES_URL).denyAll()
                        .requestMatchers(HttpMethod.GET, ROAD_URL).permitAll()
                        .requestMatchers(HttpMethod.GET, DASHBOARD_URL).permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/media").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/media/files/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/media/admin").hasRole(ADMIN)
                        .requestMatchers(HttpMethod.POST, MEDIA_URL).hasRole(ADMIN)
                        .requestMatchers(HttpMethod.PATCH, MEDIA_URL).hasRole(ADMIN)
                        .requestMatchers(HttpMethod.DELETE, MEDIA_URL).hasRole(ADMIN)
                        .requestMatchers(HttpMethod.POST, "/api/roads/*/modern-reference").hasAnyRole(USER, ADMIN)
                        .requestMatchers(HttpMethod.POST, ROAD_URL).denyAll()
                        .requestMatchers(HttpMethod.PUT, ROAD_URL).denyAll()
                        .requestMatchers(HttpMethod.DELETE, ROAD_URL).denyAll()
                        .requestMatchers(HttpMethod.POST, "/api/suggestions").hasAnyRole(USER, ADMIN)
                        .requestMatchers(HttpMethod.GET, "/api/suggestions/my").hasAnyRole(USER, ADMIN)
                        .requestMatchers(HttpMethod.GET, "/api/suggestions/pending").hasRole(ADMIN)
                        .requestMatchers(HttpMethod.PATCH, "/api/suggestions/*/review").hasRole(ADMIN)
                        .requestMatchers("/api/auth/login").permitAll()
                        .requestMatchers("/api/auth/register").hasRole(ADMIN)
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
