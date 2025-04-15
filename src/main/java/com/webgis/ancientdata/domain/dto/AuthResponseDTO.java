package com.webgis.ancientdata.domain.dto;

import java.util.List;

public record AuthResponseDTO(String token, List<String> roles, String username) {
}
