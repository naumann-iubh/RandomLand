package com.lorbeer.randomland.domain;

import org.locationtech.jts.geom.Polygon;

import java.util.List;

public record Baublock(List<Polygon> shape) {

    public static String[] HEADER() {
        return new String[]{"Shape"};
    }
}
