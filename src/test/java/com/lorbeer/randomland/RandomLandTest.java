package com.lorbeer.randomland;

import com.lorbeer.randomland.exception.NodeTreeException;
import com.lorbeer.randomland.exception.RoadGeometryException;
import com.lorbeer.randomland.services.RandomLandService;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.QuarkusTestProfile;
import io.quarkus.test.junit.TestProfile;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Map;
import java.util.Optional;

@QuarkusTest
@TestProfile(RandomLandTest.TestConfig.class)
public class RandomLandTest {
    @Inject
    RandomLandService randomLandService;

    @Test
    public void generateGpkg() throws NodeTreeException, RoadGeometryException {
        randomLandService.generate(Optional.empty(), "test", "gpkg");

        File gpkg = randomLandService.getGeopackage("test");
        Assertions.assertTrue(gpkg.exists());
    }

    @Test
    public void generateCSV() throws NodeTreeException, RoadGeometryException {
        randomLandService.generate(Optional.empty(), "test", "csv");

        File csv = randomLandService.getCSV("test");
        Assertions.assertTrue(csv.exists());
    }

    @Test
    public void generateNoExportFormat() throws NodeTreeException, RoadGeometryException {
        Assertions.assertThrows(IllegalArgumentException.class, () -> randomLandService.generate(Optional.empty(), "test", "asdf"));
    }


    public static class TestConfig implements QuarkusTestProfile {

        @Override
        public Map<String, String> getConfigOverrides() {
            return Map.ofEntries(Map.entry("city.width", "50"), Map.entry("city.height", "50"),
                    Map.entry("city.repetitions", "1"), Map.entry("city.offset", "1"), Map.entry("city.minimumRoadCount", "50"),
                    Map.entry("highway.length", "10"), Map.entry("highway.mergeThreshold", "8"),
                    Map.entry("street.mergeThreshold", "8"), Map.entry("highway.subdivideCount", "3"),
                    Map.entry("highway.scaleFactor", "1"),
                    Map.entry("flurstueck.landschaftsgartenMinGroesse", "1000.0"),
                    Map.entry("flurstueck.landschaftsgartenMaxGroesse", "3500.0"));
        }
    }
}
