package com.lorbeer.randomland.services.geometry;


import com.lorbeer.randomland.domain.Flurstueck;
import com.lorbeer.randomland.exception.NodeTreeException;
import com.lorbeer.randomland.exception.RoadGeometryException;
import com.lorbeer.randomland.generator.RoadGenerator;
import com.lorbeer.randomland.generator.domain.NodeTree;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.QuarkusTestProfile;
import io.quarkus.test.junit.TestProfile;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
@TestProfile(RoadGeometryTest.TestConfig.class)
public class RoadGeometryTest {

    @Inject
    RoadGenerator roadGenerator;

    @Inject
    RoadGeometryService roadGeometryService;

    @Test
    public void testRoadGeometry() throws NodeTreeException, RoadGeometryException {
        roadGenerator.startGeneration("test");

        Flurstueck flst = roadGeometryService.createRoadGeometry(roadGenerator.getNodeTree(), "test");
        assertEquals("Straße", flst.usage().getName());
        assertEquals(1, flst.shape().getNumGeometries());
    }

    @Test
    public void testRoadGeometryError() {

        Assertions.assertThrows(RoadGeometryException.class, () -> roadGeometryService.createRoadGeometry(null, "test"));

        Assertions.assertThrows(RoadGeometryException.class, () -> roadGeometryService.createRoadGeometry(new NodeTree(), "test"));
    }


    public static class TestConfig implements QuarkusTestProfile {

        @Override
        public Map<String, String> getConfigOverrides() {
            return Map.of("city.width", "50", "city.height", "50", "city.repetitions", "1", "city.minimumRoadCount", "50", "highway.length", "10", "highway.mergeThreshold", "8", "street.mergeThreshold", "8", "highway.subdivideCount", "3", "highway.scaleFactor", "1");
        }
    }
}
