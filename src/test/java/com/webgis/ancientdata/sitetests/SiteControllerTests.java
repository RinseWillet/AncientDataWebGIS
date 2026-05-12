package com.webgis.ancientdata.sitetests;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.webgis.ancientdata.RandomSiteGenerator;
import com.webgis.ancientdata.application.service.SiteService;
import com.webgis.ancientdata.domain.dto.ModernReferenceDTO;
import com.webgis.ancientdata.domain.dto.SiteDTO;
import com.webgis.ancientdata.domain.model.Site;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import java.util.List;
import java.util.Random;
import java.util.UUID;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SiteControllerTests {

	@Autowired
	private MockMvc mockMvc;

	private ModernReferenceDTO modernReferenceDTO;

	private List<ModernReferenceDTO> modernReferenceDTOList;

	private RandomSiteGenerator randomSiteGenerator;

	private Site site;

	private JSONObject siteJSON;

	@MockitoBean
	private SiteService siteService;

	@BeforeEach
	void setup() throws JSONException {
		randomSiteGenerator = new RandomSiteGenerator();

		Random random = new Random();
		site = randomSiteGenerator.generateRandomSite();
		site.setId(1L + random.nextInt(999));
		siteJSON = randomSiteGenerator.generateRandomSiteJSON(site);

		Long id = 1L + random.nextInt(999);
		String shortRef = UUID.randomUUID().toString();
		String fullRef = UUID.randomUUID().toString();
		String url = UUID.randomUUID().toString();

		modernReferenceDTO = new ModernReferenceDTO(id, shortRef, fullRef, url);
		modernReferenceDTOList = List.of(modernReferenceDTO);
	}

	@Test
	@WithMockUser(roles = "USER")
	void shouldAddModernReferenceToSite() throws Exception {
		ObjectMapper objectMapper = new ObjectMapper();

		when(siteService.addModernReferenceToSite(eq(site.getId()), any(ModernReferenceDTO.class)))
				.thenReturn(site);

		mockMvc.perform(post("/api/sites/" + site.getId() + "/modern-reference")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(modernReferenceDTO)))
				.andExpect(status().isOk())
				.andDo(MockMvcResultHandlers.print());

		verify(siteService, times(1)).addModernReferenceToSite(eq(site.getId()), any(ModernReferenceDTO.class));
	}

	@Test
	@WithMockUser(roles = "USER")
	void shouldForbidCreateSite() throws Exception {
		SiteDTO siteDTO = randomSiteGenerator.toDTO(site);
		ObjectMapper objectMapper = new ObjectMapper();

		mockMvc.perform(post("/api/sites")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(siteDTO)))
				.andExpect(status().isForbidden())
				.andDo(MockMvcResultHandlers.print());

		verify(siteService, times(0)).save(any(SiteDTO.class));
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	void shouldForbidDeleteSite() throws Exception {

		mockMvc.perform(delete("/api/sites/" + site.getId()))
				.andExpect(status().isForbidden())
				.andDo(MockMvcResultHandlers.print());

		verify(siteService, times(0)).delete(site.getId());
	}

	@Test
	void shouldFindAllSitesGeoJSON() throws Exception {
		when(siteService.findAllGeoJson()).thenReturn(siteJSON);

		mockMvc.perform(get("/api/sites/all")
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andDo(MockMvcResultHandlers.print());

		verify(siteService, times(1)).findAllGeoJson();
	}

	@Test
	void shouldFindModernReferencesBySiteId() throws Exception {
		when(siteService.findModernReferencesBySiteId(site.getId()))
				.thenReturn(modernReferenceDTOList);

		mockMvc.perform(get("/api/sites/modref/" + site.getId())
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andDo(MockMvcResultHandlers.print());

		verify(siteService, times(1)).findModernReferencesBySiteId(site.getId());
	}

	@Test
	void shouldFindSiteByIdGeoJSON() throws Exception {
		when(siteService.findByIdGeoJson(site.getId())).thenReturn(String.valueOf(siteJSON));

		mockMvc.perform(get("/api/sites/" + site.getId())
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andDo(MockMvcResultHandlers.print());

		verify(siteService, times(1)).findByIdGeoJson(site.getId());
	}

	@Test
	@WithMockUser(roles = "USER")
	void shouldForbidCreateSiteWhenMissingRequiredFields() throws Exception {
		String invalidJson = """
				{
				    "pleiadesId": 12345,
				    "geom": "POINT (12.4924 41.8902)"
				}
				""";

		mockMvc.perform(post("/api/sites")
						.contentType(MediaType.APPLICATION_JSON)
						.content(invalidJson))
				.andExpect(status().isForbidden())
				.andDo(MockMvcResultHandlers.print());

		verify(siteService, times(0)).save(any());
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	void shouldForbidDeletingNonexistentSite() throws Exception {

		mockMvc.perform(delete("/api/sites/9999"))
				.andExpect(status().isForbidden())
				.andDo(MockMvcResultHandlers.print());

		verify(siteService, times(0)).delete(9999L);
	}

	@Test
	@WithMockUser(roles = "USER")
	void shouldForbidUpdateSite() throws Exception {
		SiteDTO siteDTO = randomSiteGenerator.toDTO(site);
		ObjectMapper objectMapper = new ObjectMapper();

		mockMvc.perform(put("/api/sites/" + site.getId())
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(siteDTO)))
				.andExpect(status().isForbidden())
				.andDo(MockMvcResultHandlers.print());

		verify(siteService, times(0)).update(eq(site.getId()), any(SiteDTO.class));
	}

	@AfterEach
	void tearDown() {
		randomSiteGenerator = null;
		site = null;
		siteJSON = null;
		modernReferenceDTO = null;
		modernReferenceDTOList = null;
	}
}
