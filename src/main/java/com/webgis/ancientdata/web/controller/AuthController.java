package com.webgis.ancientdata.web.controller;

import com.webgis.ancientdata.application.service.AuthService;
import com.webgis.ancientdata.domain.model.Role;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody AuthRequest request) {
        String token = authService.registerUser(request.username(), request.password(), request.role());
        return ResponseEntity.ok(token);
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody AuthRequest request) {
        String token = authService.authenticate(request.username(), request.password());
        return ResponseEntity.ok(token);
    }
}

record AuthRequest(String username, String password, Role role) {
}
