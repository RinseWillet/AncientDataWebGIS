package com.webgis.ancientdata.domain.dto;

import lombok.Getter;

import java.util.List;

@Getter
public class AuthResponseDTO {
    private final String token;
    private final List<String> roles;
    private final String username;

    public AuthResponseDTO(String token, List<String> roles, String username) {
        this.token = token;
        this.roles = roles;
        this.username = username;
    }
}
