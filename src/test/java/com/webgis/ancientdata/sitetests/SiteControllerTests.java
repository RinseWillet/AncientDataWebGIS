package com.webgis.ancientdata.sitetests;

//MVC
import com.webgis.ancientdata.ancientreference.AncientReference;
import com.webgis.ancientdata.epigraphicreference.EpigraphicReference;
import com.webgis.ancientdata.modernreference.ModernReference;
import com.webgis.ancientdata.site.Site;
import com.webgis.ancientdata.site.SiteController;
import com.webgis.ancientdata.site.SiteService;

//Java
import java.util.ArrayList;
import java.util.List;

import com.webgis.ancientdata.site.SiteType;
import org.apache.commons.lang3.RandomUtils;
import org.json.JSONException;
import org.locationtech.jts.geom.*;

//Spring
import org.junit.jupiter.api.BeforeEach;
import org.locationtech.jts.geom.impl.CoordinateArraySequence;
import org.springframework.beans.factory.annotation.Autowired;

//Test boilerplate libraries
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvcBuilder;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.apache.commons.lang3.RandomStringUtils;
import org.json.JSONObject;

@ExtendWith(MockitoExtension.class)
public class SiteControllerTests {

    private Site site;
    private List<Site> siteList;
    private JSONObject siteJSON;

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
        Integer pleiadesId = RandomUtils.nextInt();
        String name = RandomStringUtils.randomAlphabetic(10);

        //creating random point
        Double x = RandomUtils.nextDouble(0, 180);
        Double y = RandomUtils.nextDouble(0, 90);
        Double z = RandomUtils.nextDouble(0, 3000);
        Coordinate coordinate = new Coordinate(x,y,z);
        Coordinate [] coordinates = new Coordinate[]{coordinate};
        CoordinateArraySequence coordinateArraySequence = new CoordinateArraySequence(coordinates);
        GeometryFactory geometryFactory = new GeometryFactory();
        Point geom = new Point(coordinateArraySequence, geometryFactory);

        String province = RandomStringUtils.randomAlphabetic(10);
        String siteType = RandomStringUtils.randomAlphabetic(10);
        String status = RandomStringUtils.randomAlphabetic(10);
        String statusReference = RandomStringUtils.randomAlphabetic(10);
        String comment = RandomStringUtils.randomAlphabetic(10);

//        //Setting up modern reference
//        ModernReference modernReference = new ModernReference(
//                RandomStringUtils.randomAlphabetic(10),
//                RandomUtils.nextInt(),
//                RandomStringUtils.randomAlphabetic(10)
//        );
//
//        //Setting up ancient reference
//        AncientReference ancientReference = new AncientReference(
//                RandomStringUtils.randomAlphabetic(10),
//                RandomStringUtils.randomAlphabetic(10),
//                RandomStringUtils.randomAlphabetic(10),
//                RandomStringUtils.randomAlphabetic(10),
//                RandomUtils.nextInt()
//        );
//
//        //Setting up references
//        EpigraphicReference epigraphicReference = new EpigraphicReference(
//                RandomStringUtils.randomAlphabetic(10),
//                RandomStringUtils.randomAlphabetic(10),
//                RandomStringUtils.randomAlphabetic(10),
//                RandomUtils.nextInt(),
//                RandomStringUtils.randomAlphabetic(10)
//        );
//
//        ArrayList<ModernReference> modernReferences = new ArrayList<>();
//        ArrayList<AncientReference> ancientReferences = new ArrayList<>();
//        ArrayList<EpigraphicReference> epigraphicReferences = new ArrayList<>();
//        modernReferences.add(modernReference);
//        ancientReferences.add(ancientReference);
//        epigraphicReferences.add(epigraphicReference);

        site = new Site(pleiadesId,
                name,
                geom,
                province,
                siteType,
                status,
                statusReference,
                comment);
        siteList.add(site);

        siteJSON = new JSONObject();
        siteJSON.put("pleiadesId", pleiadesId);
        siteJSON.put("name", name);
        siteJSON.put("geom", geom);
        siteJSON.put("province", province);
        siteJSON.put("siteType", siteType);
        siteJSON.put("status", status);
        siteJSON.put("statusReference", statusReference);
        siteJSON.put("comment", comment);
    }

    @AfterEach
    void tearDown() {
        site = null;
        siteList = null;
        siteJSON = null;
    }

    @Test
    public void shouldFindAllSites() throws Exception {
        when(siteService.findAll()).thenReturn(siteList);

        mockMvc.perform(get("/api/sites/all")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.valueOf(siteJSON)))
                .andExpect(status().isOk())
                .andDo(MockMvcResultHandlers.print());

        verify(siteService, times(1)).findAll();
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
}
