package com.webgis.ancientdata.web.controller;

import com.webgis.ancientdata.application.service.AuthService;
import com.webgis.ancientdata.domain.dto.AuthResponseDTO;
import com.webgis.ancientdata.domain.model.Role;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponseDTO> register(@RequestBody AuthRequest request) {
        Role effectiveRole = request.role() == null ? Role.USER : request.role();
        String token = authService.registerUser(request.username(), request.password(), effectiveRole);
        return ResponseEntity.ok(new AuthResponseDTO(token, List.of(effectiveRole.name())));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@RequestBody AuthRequest request) {
        return ResponseEntity.ok(authService.login(request.username(), request.password()));
    }

    public record AuthRequest(String username, String password, Role role) {
    }
}

