package com.webgis.ancientdata.domain.repository;

import com.webgis.ancientdata.domain.model.DataSuggestion;
import com.webgis.ancientdata.domain.model.SuggestionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DataSuggestionRepository extends JpaRepository<DataSuggestion, Long> {
    List<DataSuggestion> findByStatusOrderByCreatedAtAsc(SuggestionStatus status);

    List<DataSuggestion> findBySubmitterUsernameOrderByCreatedAtDesc(String submitterUsername);
}

