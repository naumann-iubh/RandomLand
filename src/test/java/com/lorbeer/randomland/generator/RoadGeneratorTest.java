package com.lorbeer.randomland.generator;

import com.lorbeer.randomland.exception.NodeTreeException;
import com.lorbeer.randomland.generator.domain.RoadType;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.QuarkusTestProfile;
import io.quarkus.test.junit.TestProfile;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.math.Vector2D;

import java.util.Map;

@QuarkusTest
@TestProfile(RoadGeneratorTest.TestConfig.class)
public class RoadGeneratorTest {

    @Inject
    RoadGenerator roadGenerator;

    @Test
    public void testGenerate() throws NodeTreeException {
        roadGenerator.startGeneration("test");
        Assertions.assertFalse(roadGenerator.getNodeTree().getNodeEdges().isEmpty());
    }

    @Test
    public void addNodeError() throws NodeTreeException {
        roadGenerator.startGeneration("test");

        Assertions.assertThrows(NodeTreeException.class, () ->
                roadGenerator.getNodeTree().addNode(new Vector2D(10000000, 1000000), RoadType.STREET));
    }

    public static class TestConfig implements QuarkusTestProfile {

        @Override
        public Map<String, String> getConfigOverrides() {
            return Map.of("city.width", "50", "city.height", "50", "city.repetitions", "1", "city.minimumRoadCount", "50", "highway.length", "10", "highway.mergeThreshold", "8", "street.mergeThreshold", "8", "highway.subdivideCount", "3", "highway.scaleFactor", "1");
        }
    }
}
