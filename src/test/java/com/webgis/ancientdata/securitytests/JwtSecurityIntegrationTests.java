package com.webgis.ancientdata.securitytests;

import com.webgis.ancientdata.application.service.RoadService;
import com.webgis.ancientdata.domain.dto.RoadDTO;
import com.webgis.ancientdata.domain.model.Role;
import com.webgis.ancientdata.domain.model.Road;
import com.webgis.ancientdata.domain.model.User;
import com.webgis.ancientdata.domain.repository.UserRepository;
import com.webgis.ancientdata.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.io.WKTReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class JwtSecurityIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtil jwtUtil;

    @MockitoBean
    private RoadService roadService;

    @MockitoBean
    private UserRepository userRepository;

    @Test
    void shouldRejectProtectedCreateRoadWithoutJwt() throws Exception {
        mockMvc.perform(post("/api/roads")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRoadPayload()))
                .andExpect(status().isUnauthorized());

        verify(roadService, never()).save(any(RoadDTO.class));
    }

    @Test
    void shouldAllowProtectedCreateRoadWithValidJwt() throws Exception {
        User user = new User();
        user.setUsername("jwt-user");
        user.setPassword("encoded-password");
        user.setRole(Role.USER);

        when(userRepository.findByUsername("jwt-user")).thenReturn(Optional.of(user));
        when(roadService.save(any(RoadDTO.class))).thenReturn(buildSavedRoad());

        String token = jwtUtil.generateToken("jwt-user", "USER");

        mockMvc.perform(post("/api/roads")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRoadPayload()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Via Nova"))
                .andExpect(jsonPath("$.type").value("road"));

        verify(roadService).save(any(RoadDTO.class));
    }

    private String validRoadPayload() {
        return """
                {
                  "cat_nr": 42,
                  "name": "Via Nova",
                  "geom": "MULTILINESTRING ((10 10, 11 11))",
                  "type": "road"
                }
                """;
    }

    private Road buildSavedRoad() throws Exception {
        MultiLineString geometry = (MultiLineString) new WKTReader().read("MULTILINESTRING ((10 10, 11 11))");
        Road road = new Road();
        road.setId(99L);
        road.setCat_nr(42);
        road.setName("Via Nova");
        road.setGeom(geometry);
        road.setType("road");
        return road;
    }
}

