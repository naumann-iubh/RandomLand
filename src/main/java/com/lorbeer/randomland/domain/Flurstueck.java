package com.lorbeer.randomland.domain;

import org.locationtech.jts.geom.Geometry;

public record Flurstueck(Geometry shape, NutungsartFlurstueck usage) {
}
