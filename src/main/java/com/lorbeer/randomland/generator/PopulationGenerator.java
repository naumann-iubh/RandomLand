package com.lorbeer.randomland.generator;

import com.lorbeer.randomland.util.OpenSimplex2S;
import jakarta.inject.Singleton;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.awt.image.BufferedImage;
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

    private long seed;

    public void generatePopulationHeatMap(Optional<Long> seedOptional) {
        seed = seedOptional.orElse(System.currentTimeMillis());
    }

    public float getPopulationAtPosition(double x, double y) {
        return OpenSimplex2S.noise2(seed, x * offset, y * offset);
    }

    public BufferedImage getImage() {
        final BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                final float noise = OpenSimplex2S.noise2(seed, x * offset, y * offset);
                int value = (int) (255 * noise);
                if (value < 0) {
                    value = 0;
                }
                final int rgb = value << 16 | value << 8 | value;
                image.setRGB(x, y, rgb);
            }
        }
        return image;
    }
}
