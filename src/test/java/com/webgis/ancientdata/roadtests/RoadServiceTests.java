package com.webgis.ancientdata.roadtests;

import com.webgis.ancientdata.road.Road;
import com.webgis.ancientdata.road.RoadRepository;
import com.webgis.ancientdata.road.RoadService;


import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.geom.impl.CoordinateArraySequence;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RoadServiceTests {

    private Road road;
    private List<Road> roadList;

    @Mock
    private RoadRepository roadRepository;

    @Autowired
    @InjectMocks
    private RoadService roadService;

    @BeforeEach
    public void setUp(){

        roadList = new ArrayList<>();
        int cat_nr = RandomUtils.nextInt();
        String name = RandomStringUtils.randomAlphabetic(10);

        //creating random points and line
        Integer randomAmountLines = RandomUtils.nextInt(2, 500);
        LineString[] lineStringArray = new LineString[randomAmountLines];

        for (int i = 0; i < randomAmountLines; i++) {
            Integer randomLinePoints = RandomUtils.nextInt(2, 10);

            Coordinate [] points = new Coordinate[randomLinePoints];

            for (int j = 0; j < randomLinePoints; j++){
                Double x = RandomUtils.nextDouble(0, 180);
                Double y = RandomUtils.nextDouble(0, 90);
                Double z = RandomUtils.nextDouble(0, 3000);
                Coordinate coordinate = new Coordinate(x,y,z);
                points[j] = coordinate;
            }

            CoordinateSequence coordinateArraySequence = new CoordinateArraySequence(points);
            GeometryFactory geometryFactory_lineString = new GeometryFactory();
            LineString lineString = new LineString(coordinateArraySequence, geometryFactory_lineString);
            lineStringArray[i] = lineString;
        }

        GeometryFactory geometryFactory_multiLineString = new GeometryFactory();
        MultiLineString geom = new MultiLineString(lineStringArray, geometryFactory_multiLineString);

        String type = RandomStringUtils.randomAlphabetic(10);
        String typeDescription = RandomStringUtils.randomAlphabetic(15);
        String location = RandomStringUtils.randomAlphabetic(100);
        String description = RandomStringUtils.randomAlphabetic(100);
        String date = RandomStringUtils.randomAlphabetic(10);
        String references = RandomStringUtils.randomAlphabetic(50);
        String historicalReferences = RandomStringUtils.randomAlphabetic(50);


        Road road = new Road(cat_nr,
                name,
                geom,
                type,
                typeDescription,
                location,
                description,
                date,
                references,
                historicalReferences);

        roadList.add(road);
    }

    @AfterEach
    public void tearDown() {
        road = null;
        roadList = null;
    }

    @Test
    public void shouldListAllRoads() {
        when(roadRepository.findAll()).thenReturn(roadList);

        List<Road> fetchedRoads = (List<Road>) roadService.findAll();
        assertEquals(fetchedRoads, roadList);

        verify(roadRepository, times(1)).findAll();
    }
}
