package com.webgis.ancientdata.application.service;

import com.webgis.ancientdata.domain.model.Role;
import com.webgis.ancientdata.domain.model.User;
import com.webgis.ancientdata.domain.repository.UserRepository;
import com.webgis.ancientdata.security.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public String registerUser(String username, String password, Role role) {
        if (userRepository.findByUsername(username).isPresent()) {
            logger.warn("Registration failed: User '{}' already exists", username);
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User already exists");
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role);

        userRepository.save(user);
        logger.info("User '{}' successfully registered with role '{}'", username, role);
        return jwtUtil.generateToken(username, role.name());
    }

    public String authenticate(String username, String password) {
        Optional<User> userOptional = userRepository.findByUsername(username);
        if (userOptional.isPresent() && passwordEncoder.matches(password, userOptional.get().getPassword())) {
            logger.info("User '{}' successfully authenticated", username);
            return jwtUtil.generateToken(username, userOptional.get().getRole().name());
        }
        logger.warn("Authentication failed for user '{}'", username);
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
    }
}
