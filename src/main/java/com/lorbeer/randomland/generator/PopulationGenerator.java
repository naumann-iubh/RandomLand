package com.lorbeer.randomland.generator;

import com.lorbeer.randomland.util.OpenSimplex2S;
import jakarta.inject.Singleton;
import org.apache.commons.collections.map.HashedMap;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.awt.image.BufferedImage;
import java.util.Map;
import java.util.Optional;

@Singleton
public class PopulationGenerator {
    private static final Logger Log = Logger.getLogger(PopulationGenerator.class);
    @ConfigProperty(name = "city.width")
    int width;
    @ConfigProperty(name = "city.height")
    int height;
    @ConfigProperty(name = "city.offset")
    Double offset;

    private static Map<String, Long> seeds = new HashedMap();

    private Long seed;

    public void generatePopulationHeatMap(Optional<Long> seedOptional, String uuid) {
        if (seeds.containsKey(uuid)) {
            seed = seeds.get(uuid);
        } else {
            seed = seedOptional.orElse(System.currentTimeMillis());
            seeds.put(uuid, seed);
        }
    }

    public float getPopulationAtPosition(double x, double y) {
        return OpenSimplex2S.noise2_ImproveX(seed, x * offset, y * offset);
    }

    public Optional<BufferedImage> getImage(String uuid) {
        if (seeds.containsKey(uuid)) {
            final BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    final float noise = OpenSimplex2S.noise2_ImproveX(seeds.get(uuid), x * offset, y * offset);
                    int value = (int) (255 * noise);
                    if (value < 0) {
                        value = 0;
                    }
                    final int rgb = value << 16 | value << 8 | value;
                    image.setRGB(x, y, rgb);
                }
            }
            return Optional.of(image);
        }
        return Optional.empty();
    }
}
