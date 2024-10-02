package com.lorbeer.randomland.services.geometry;

import com.lorbeer.randomland.domain.Flurstueck;
import com.lorbeer.randomland.domain.NutungsartFlurstueck;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.locationtech.jts.geom.Polygon;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@ApplicationScoped
public class FlurstueckUndNutzungsService {

    @ConfigProperty(name = "flurstueck.landschaftsgartenMinGroesse")
    Double landschaftsgartenMinGroesse;

    @ConfigProperty(name = "flurstueck.landschaftsgartenMaxGroesse")
    Double landschaftsgartenMaxGroesse;

    private final Random rnd = new Random();
    private final Integer chanceByForPark = 5;

    public List<Flurstueck> createFlurstueckundNutzung(List<Polygon> polygon) {

        final List<Flurstueck> flurstueckList = new ArrayList<>();

        for (Polygon pol : polygon) {
            if (pol.getArea() <= landschaftsgartenMinGroesse && pol.getArea() < landschaftsgartenMaxGroesse && rnd.nextInt(100) + 1 == chanceByForPark) {
                flurstueckList.add(new Flurstueck(pol, NutungsartFlurstueck.PARK));
            } else {
                flurstueckList.add(new Flurstueck(pol, NutungsartFlurstueck.GEBAUDE));
            }
        }
        return flurstueckList;
    }

}
