package com.webgis.ancientdata.application.service;

import com.webgis.ancientdata.domain.dto.SuggestionCreateRequestDTO;
import com.webgis.ancientdata.domain.dto.SuggestionResponseDTO;
import com.webgis.ancientdata.domain.dto.SuggestionReviewRequestDTO;
import com.webgis.ancientdata.domain.model.DataSuggestion;
import com.webgis.ancientdata.domain.model.SuggestionStatus;
import com.webgis.ancientdata.domain.model.SuggestionTargetType;
import com.webgis.ancientdata.domain.repository.DataSuggestionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Service
@Transactional
public class SuggestionService {

    private final DataSuggestionRepository repository;

    public SuggestionService(DataSuggestionRepository repository) {
        this.repository = repository;
    }

    public SuggestionResponseDTO submitSuggestion(String username, SuggestionCreateRequestDTO request) {
        DataSuggestion suggestion = new DataSuggestion();
        suggestion.setTargetType(parseTargetType(request.targetType()));
        suggestion.setTargetId(request.targetId());
        suggestion.setSummary(request.summary().trim());
        suggestion.setDetails(request.details().trim());
        suggestion.setImageUrl(blankToNull(request.imageUrl()));
        suggestion.setStatus(SuggestionStatus.PENDING);
        suggestion.setSubmitterUsername(username);

        DataSuggestion saved = repository.save(suggestion);
        return toDto(saved);
    }

    @Transactional(readOnly = true)
    public List<SuggestionResponseDTO> listMySuggestions(String username) {
        return repository.findBySubmitterUsernameOrderByCreatedAtDesc(username)
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SuggestionResponseDTO> listPendingSuggestions() {
        return repository.findByStatusOrderByCreatedAtAsc(SuggestionStatus.PENDING)
                .stream()
                .map(this::toDto)
                .toList();
    }

    public SuggestionResponseDTO reviewSuggestion(Long id, String reviewer, SuggestionReviewRequestDTO request) {
        DataSuggestion suggestion = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Suggestion not found"));

        SuggestionStatus decision = parseReviewDecision(request.decision());
        suggestion.setStatus(decision);
        suggestion.setReviewerNotes(blankToNull(request.reviewerNotes()));
        suggestion.setReviewedBy(reviewer);
        suggestion.setReviewedAt(LocalDateTime.now());

        return toDto(repository.save(suggestion));
    }

    private SuggestionTargetType parseTargetType(String rawType) {
        try {
            return SuggestionTargetType.valueOf(rawType.trim().toUpperCase(Locale.ROOT));
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid targetType. Use ROAD, SITE, or GENERAL.");
        }
    }

    private SuggestionStatus parseReviewDecision(String rawDecision) {
        try {
            SuggestionStatus status = SuggestionStatus.valueOf(rawDecision.trim().toUpperCase(Locale.ROOT));
            if (status == SuggestionStatus.PENDING) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Decision must be APPROVED or REJECTED.");
            }
            return status;
        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid decision. Use APPROVED or REJECTED.");
        }
    }

    private String blankToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private SuggestionResponseDTO toDto(DataSuggestion suggestion) {
        return new SuggestionResponseDTO(
                suggestion.getId(),
                suggestion.getTargetType(),
                suggestion.getTargetId(),
                suggestion.getSummary(),
                suggestion.getDetails(),
                suggestion.getImageUrl(),
                suggestion.getStatus(),
                suggestion.getSubmitterUsername(),
                suggestion.getReviewerNotes(),
                suggestion.getReviewedBy(),
                suggestion.getCreatedAt(),
                suggestion.getReviewedAt()
        );
    }
}

