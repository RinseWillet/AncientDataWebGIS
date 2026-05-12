package com.webgis.ancientdata.web.controller;

import com.webgis.ancientdata.application.service.SuggestionService;
import com.webgis.ancientdata.domain.dto.SuggestionCreateRequestDTO;
import com.webgis.ancientdata.domain.dto.SuggestionResponseDTO;
import com.webgis.ancientdata.domain.dto.SuggestionReviewRequestDTO;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/suggestions")
public class SuggestionController {

    private final SuggestionService suggestionService;

    public SuggestionController(SuggestionService suggestionService) {
        this.suggestionService = suggestionService;
    }

    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<SuggestionResponseDTO> submitSuggestion(
            Authentication authentication,
            @Valid @RequestBody SuggestionCreateRequestDTO request
    ) {
        return ResponseEntity.ok(suggestionService.submitSuggestion(authentication.getName(), request));
    }

    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @GetMapping("/my")
    public ResponseEntity<List<SuggestionResponseDTO>> listMySuggestions(Authentication authentication) {
        return ResponseEntity.ok(suggestionService.listMySuggestions(authentication.getName()));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/pending")
    public ResponseEntity<List<SuggestionResponseDTO>> listPendingSuggestions() {
        return ResponseEntity.ok(suggestionService.listPendingSuggestions());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/review")
    public ResponseEntity<SuggestionResponseDTO> reviewSuggestion(
            @PathVariable Long id,
            Authentication authentication,
            @Valid @RequestBody SuggestionReviewRequestDTO request
    ) {
        return ResponseEntity.ok(suggestionService.reviewSuggestion(id, authentication.getName(), request));
    }
}

