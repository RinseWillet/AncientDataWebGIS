package com.webgis.ancientdata.domain.dto;

import java.util.List;

public class AuthResponseDTO {
    private String token;
    private List<String> roles;

    public AuthResponseDTO(String token, List<String> roles) {
        this.token = token;
        this.roles = roles;
    }

    public String getToken() {
        return token;
    }

    public List<String> getRoles() {
        return roles;
    }
}
