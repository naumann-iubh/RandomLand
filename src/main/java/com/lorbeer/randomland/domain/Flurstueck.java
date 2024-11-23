package com.lorbeer.randomland.domain;

import org.locationtech.jts.geom.Geometry;

public record Flurstueck(Geometry shape, NutungsartFlurstueck usage) {

    public static String[] HEADER() {
        return new String[]{"WKT", "Nutzungsart"};
    }

}
