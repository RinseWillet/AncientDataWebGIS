package com.webgis.ancientdata.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ModernReferenceDTO {

    private Long id;

    @NotBlank(message = "Short reference is required")
    @Size(max = 255, message = "Short reference must be less than 255 characters")
    private String shortRef;

    @NotBlank(message = "Full reference is required")
    @Size(max = 1000, message = "Full reference must be less than 1000 characters")
    private String fullRef;

    @Size(max = 1000, message = "URL must be less than 1000 characters")
    private String url;
}