package com.lorbeer.randomland.services.geometry;

import com.lorbeer.randomland.domain.Baublock;
import com.lorbeer.randomland.domain.Flurstueck;
import com.lorbeer.randomland.exception.NodeTreeException;
import com.lorbeer.randomland.exception.RoadGeometryException;
import com.lorbeer.randomland.generator.RoadGenerator;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.QuarkusTestProfile;
import io.quarkus.test.junit.TestProfile;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;

@QuarkusTest
@TestProfile(BaubloeckeServiceTest.TestConfig.class)
public class BaubloeckeServiceTest {

    @Inject
    BaubloeckeService service;

    @Inject
    RoadGenerator roadGenerator;

    @Inject
    RoadGeometryService roadGeometryService;

    @Test
    public void createBaubloecke() throws NodeTreeException, RoadGeometryException {
        roadGenerator.startGeneration("test");
        Flurstueck flst = roadGeometryService.createRoadGeometry(roadGenerator.getNodeTree(), "test");
        Baublock bb = service.createBaubloecke(flst.shape());
        Assertions.assertFalse(bb.shape().isEmpty());
    }

    public static class TestConfig implements QuarkusTestProfile {

        @Override
        public Map<String, String> getConfigOverrides() {
            return Map.of("city.width", "50", "city.height", "50", "city.repetitions", "1", "city.minimumRoadCount", "50", "highway.length", "10", "highway.mergeThreshold", "8", "street.mergeThreshold", "8", "highway.subdivideCount", "3", "highway.scaleFactor", "1");
        }
    }

}
