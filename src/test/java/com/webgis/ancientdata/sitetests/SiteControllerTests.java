package com.webgis.ancientdata.sitetests;

//MVC

import com.webgis.ancientdata.RandomSiteGenerator;
import com.webgis.ancientdata.application.service.SiteService;
import com.webgis.ancientdata.domain.dto.ModernReferenceDTO;
import com.webgis.ancientdata.domain.model.Site;
import com.webgis.ancientdata.web.controller.SiteController;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class SiteControllerTests {

    private RandomSiteGenerator randomSiteGenerator;
    private Site site;
    private List<Site> siteList;
    private JSONObject siteJSON;
    private ModernReferenceDTO modernReferenceDTO;
    private List<ModernReferenceDTO> modernReferenceDTOList;

    @Mock
    private SiteService siteService;

    @InjectMocks
    private SiteController siteController;

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    public void setup() throws JSONException {
        mockMvc = MockMvcBuilders.standaloneSetup(siteController).build();

        siteList = new ArrayList<>();
        randomSiteGenerator = new RandomSiteGenerator();

        site = randomSiteGenerator.generateRandomSite();
        site.setId(RandomUtils.nextLong(1, 1000));
        siteList.add(site);
        siteJSON = randomSiteGenerator.generateRandomSiteJSON(site);

        modernReferenceDTOList = new ArrayList<>();

        Long id = RandomUtils.nextLong();
        String shortRef = RandomStringUtils.randomAlphabetic(100);
        String fullRef = RandomStringUtils.randomAlphabetic(100);
        String URL = RandomStringUtils.randomAlphabetic(100);

        modernReferenceDTO = new ModernReferenceDTO(id, shortRef, fullRef, URL);
        modernReferenceDTOList.add(modernReferenceDTO);
    }

    @AfterEach
    void tearDown() {
        randomSiteGenerator = null;
        site = null;
        siteList = null;
        siteJSON = null;
        modernReferenceDTO = null;
        modernReferenceDTOList = null;
    }

    @Test
    public void shouldFindSiteByIdGeoJSON() throws Exception {
        when(siteService.findByIdGeoJson(site.getId())).thenReturn(String.valueOf(siteJSON));

        mockMvc.perform(get("/api/sites/" + site.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.valueOf(siteJSON)))
                .andExpect(status().isOk())
                .andDo(MockMvcResultHandlers.print());

        verify(siteService, times(1)).findByIdGeoJson(site.getId());
    }

    @Test
    public void shouldFindAllSitesGeoJSON() throws Exception {
        when(siteService.findAllGeoJson()).thenReturn(siteJSON);

        mockMvc.perform(get("/api/sites/all")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.valueOf(siteJSON)))
                .andExpect(status().isOk())
                .andDo(MockMvcResultHandlers.print());

        verify(siteService, times(1)).findAllGeoJson();
    }

    @Test
    public void shouldFindModernReferencesBySiteId() throws Exception {
        when(siteService.findModernReferencesBySiteId(site.getId())).thenReturn(modernReferenceDTOList);

        mockMvc.perform(get("/api/sites/modref/" + site.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(modernReferenceDTOList.toString()))
                .andExpect(status().isOk())
                .andDo(MockMvcResultHandlers.print());

        verify(siteService, times(1)).findModernReferencesBySiteId(site.getId());
    }
}
