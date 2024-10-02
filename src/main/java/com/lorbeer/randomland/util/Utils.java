package com.lorbeer.randomland.util;

import org.jboss.logging.Logger;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineSegment;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;

import java.util.ArrayList;
import java.util.List;

public final class Utils {
    private static final Logger Log = Logger.getLogger(Utils.class);

    public static boolean isDuplicateByGeometry(List<Geometry> geometryList, Geometry newGeometry) {
        for (Geometry geometry : geometryList) {
            if (geometry.contains(newGeometry)) {
                return true;
            } else if (newGeometry.contains(geometry)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isDuplicateByLineSegment(List<LineSegment> segments, LineSegment newSegment) {
        for (LineSegment segment : segments) {
            if (segment.equals(newSegment)) {
                return true;
            }
        }
        return false;
    }

    public static List<Polygon> iterateThroughMultiPolygon(MultiPolygon multiPolygon) {
        final List<Polygon> polygons = new ArrayList<>();
        for (int i = 0; i < multiPolygon.getNumGeometries(); i++) {
            final Geometry g = multiPolygon.getGeometryN(i);
            if (g instanceof MultiPolygon) {
                polygons.addAll(iterateThroughMultiPolygon((MultiPolygon) g));
            } else {
                Log.debug("type after Mulitpolygon check " + g.getGeometryType());
                polygons.add((Polygon) g);
            }
        }
        return polygons;
    }
}
