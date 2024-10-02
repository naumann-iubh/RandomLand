package com.lorbeer.randomland.services.geopackage;

import org.locationtech.jts.geom.Geometry;

public record GeometryInfo(Geometry geometry, Type type, String id) {
}
