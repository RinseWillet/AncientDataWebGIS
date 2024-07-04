package com.webgis.ancientdata.sitetests;

//MVC
import com.webgis.ancientdata.ancientreference.AncientReference;
import com.webgis.ancientdata.epigraphicreference.EpigraphicReference;
import com.webgis.ancientdata.modernreference.ModernReference;
import com.webgis.ancientdata.site.*;

//Java
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.impl.CoordinateArraySequence;
import org.springframework.beans.factory.annotation.Autowired;

//Test boilerplate libraries
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SiteServiceTests {
    private Site site;
    private List<Site> siteList;

    @Mock
    private SiteRepository siteRepository;

    @Autowired
    @InjectMocks
    private SiteService siteService;

    @BeforeEach
    public void setUp(){
        siteList = new ArrayList<>();

        //generating values for fields
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
        SiteType siteType = SiteType.VILLA;
        String conventus = RandomStringUtils.randomAlphabetic(10);
        String status = RandomStringUtils.randomAlphabetic(10);
        String statusReference = RandomStringUtils.randomAlphabetic(10);
        String comment = RandomStringUtils.randomAlphabetic(10);

        //setting up modern reference
        ModernReference modernReference = new ModernReference(
                RandomStringUtils.randomAlphabetic(10),
                RandomUtils.nextInt(),
                RandomStringUtils.randomAlphabetic(10)
        );

        //setting up ancient reference
        AncientReference ancientReference = new AncientReference(
                RandomStringUtils.randomAlphabetic(10),
                RandomStringUtils.randomAlphabetic(10),
                RandomStringUtils.randomAlphabetic(10),
                RandomStringUtils.randomAlphabetic(10),
                RandomUtils.nextInt()
        );

        //setting up references
        EpigraphicReference epigraphicReference = new EpigraphicReference(
                RandomStringUtils.randomAlphabetic(10),
                RandomStringUtils.randomAlphabetic(10),
                RandomStringUtils.randomAlphabetic(10),
                RandomUtils.nextInt(),
                RandomStringUtils.randomAlphabetic(10)
        );

        ArrayList<ModernReference> modernReferences = new ArrayList<>();
        ArrayList<AncientReference> ancientReferences = new ArrayList<>();
        ArrayList<EpigraphicReference> epigraphicReferences = new ArrayList<>();
        modernReferences.add(modernReference);
        ancientReferences.add(ancientReference);
        epigraphicReferences.add(epigraphicReference);

        site = new Site(pleiadesId,
                name,
                geom,
                province,
                siteType,
                conventus,
                status,
                statusReference,
                comment,
                modernReferences,
                ancientReferences,
                epigraphicReferences);
        siteList.add(site);
    }

    @AfterEach
    public void tearDown() {
        site = null;
        siteList = null;
    }

    @Test
    public void shouldListSites() {
        when(siteRepository.findAll()).thenReturn(siteList);

        List<Site> fetchedUsers = (List<Site>) siteService.findAll();
        assertEquals(fetchedUsers, siteList);

        verify(siteRepository, times(1)).findAll();
    }

}
