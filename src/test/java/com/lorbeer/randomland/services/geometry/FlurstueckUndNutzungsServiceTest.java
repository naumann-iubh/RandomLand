package com.lorbeer.randomland.services.geometry;


import com.lorbeer.randomland.domain.Baublock;
import com.lorbeer.randomland.domain.Flurstueck;
import com.lorbeer.randomland.exception.NodeTreeException;
import com.lorbeer.randomland.exception.RoadGeometryException;
import com.lorbeer.randomland.generator.PopulationGenerator;
import com.lorbeer.randomland.generator.RoadGenerator;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.QuarkusTestProfile;
import io.quarkus.test.junit.TestProfile;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@QuarkusTest
@TestProfile(FlurstueckUndNutzungsServiceTest.TestConfig.class)
public class FlurstueckUndNutzungsServiceTest {

    @Inject
    BaubloeckeService service;

    @Inject
    RoadGenerator roadGenerator;

    @Inject
    RoadGeometryService roadGeometryService;

    @Inject
    FlurstueckUndNutzungsService flurstueckUndNutzungsService;

    @Inject
    PopulationGenerator populationGenerator;

    @BeforeEach
    public void init() {
        populationGenerator.generatePopulationHeatMap(Optional.empty(), "test");
    }


    @Test
    public void createFlurstueckundNutzung() throws NodeTreeException, RoadGeometryException {
        roadGenerator.startGeneration("test");
        Flurstueck flst = roadGeometryService.createRoadGeometry(roadGenerator.getNodeTree(), "test");
        Baublock bb = service.createBaubloecke(flst.shape());
        Assertions.assertFalse(bb.shape().isEmpty());
        List<Flurstueck> flstList = flurstueckUndNutzungsService.createFlurstueckundNutzung(bb.shape());
        Assertions.assertFalse(flstList.isEmpty());
    }


    public static class TestConfig implements QuarkusTestProfile {

        @Override
        public Map<String, String> getConfigOverrides() {
            return Map.ofEntries(Map.entry("city.width", "50"), Map.entry("city.height", "50"),
                    Map.entry("city.repetitions", "1"), Map.entry("city.minimumRoadCount", "50"),
                    Map.entry("highway.length", "10"), Map.entry("highway.mergeThreshold", "8"),
                    Map.entry("street.mergeThreshold", "8"), Map.entry("highway.subdivideCount", "3"),
                    Map.entry("highway.scaleFactor", "1"),
                    Map.entry("flurstueck.landschaftsgartenMinGroesse", "1000.0"),
                    Map.entry("flurstueck.landschaftsgartenMaxGroesse", "3500.0"));
        }
    }
}
