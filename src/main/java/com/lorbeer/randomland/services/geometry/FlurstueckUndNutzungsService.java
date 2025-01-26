package com.lorbeer.randomland.services.geometry;

import com.lorbeer.randomland.domain.Flurstueck;
import com.lorbeer.randomland.domain.NutungsartFlurstueck;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.jboss.logging.Logger;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.operation.union.CascadedPolygonUnion;
import org.locationtech.jts.triangulate.DelaunayTriangulationBuilder;
import org.locationtech.jts.triangulate.VoronoiDiagramBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@ApplicationScoped
public class FlurstueckUndNutzungsService {
    private static final Logger Log = Logger.getLogger(FlurstueckUndNutzungsService.class);

    @ConfigProperty(name = "flurstueck.landschaftsgartenMinGroesse")
    Double landschaftsgartenMinGroesse;

    @ConfigProperty(name = "flurstueck.landschaftsgartenMaxGroesse")
    Double landschaftsgartenMaxGroesse;

    @ConfigProperty(name = "flurstueck.landschaftsgartenProzentualerAnteil")
    Double landschaftsgartenProzentualerAnteil;

    private final Random rnd = new Random();
    final GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();

    public List<Flurstueck> createFlurstueckundNutzung(List<Polygon> polygon) {

        final List<Flurstueck> flurstueckList = new ArrayList<>();

        for (Polygon pol : polygon) {
            Log.info("createFlurstueckundNutzung " + pol.getArea());
            if (pol.getArea() <= landschaftsgartenMinGroesse && pol.getArea() < landschaftsgartenMaxGroesse && rnd.nextInt(100) + 1 == landschaftsgartenProzentualerAnteil) {
                flurstueckList.add(new Flurstueck(pol, NutungsartFlurstueck.PARK));
            } else {
                if (pol.getArea() < 80) {
                    flurstueckList.add(new Flurstueck(pol, NutungsartFlurstueck.GEBAUDE));
                } else {
                    final List<Geometry> divided = divide(pol);
                    for (Geometry geom : divided) {
                        flurstueckList.add(new Flurstueck(geom, NutungsartFlurstueck.GEBAUDE));
                    }
                }
            }
        }
        return flurstueckList;
    }


    private List<Geometry> divide(Polygon polygon) {
        Log.info("divide " + polygon.getNumGeometries());
        final List<Geometry> divided = new ArrayList<>();
        final DelaunayTriangulationBuilder delaunayTriangulationBuilder = new DelaunayTriangulationBuilder();
        delaunayTriangulationBuilder.setSites(polygon);
        final Geometry triangulation = delaunayTriangulationBuilder.getTriangles(geometryFactory);
        Log.info("divide delauny");
        final VoronoiDiagramBuilder voronoiDiagramBuilder = new VoronoiDiagramBuilder();
        Log.info("divide voronoi 1");
        voronoiDiagramBuilder.setClipEnvelope(polygon.getEnvelopeInternal());
        Log.info("divide voronoi 2");
        List<Coordinate> centerCoords = new ArrayList<>();
        for (int i = 0; i < triangulation.getNumGeometries(); i++) {
            centerCoords.add(triangulation.getGeometryN(i).getCentroid().getCoordinate());
        }
        Log.info("divide voronoi 3");
        voronoiDiagramBuilder.setSites(centerCoords);
        Log.info("divide voronoi 4");
        final Geometry voronoi = voronoiDiagramBuilder.getDiagram(geometryFactory);
        Log.info("divide voronoi 5");
        final Geometry tailored = voronoi.intersection(polygon);
        Log.info("divide voronoi 6");
        for (int i = 0; i < tailored.getNumGeometries(); i++) {
            divided.add(tailored.getGeometryN(i));
        }

        return mergeTooSmallPolygons(divided);
    }

    private List<Geometry> mergeTooSmallPolygons(List<Geometry> polygons) {
        Log.info("mergeTooSmallPolygons " + polygons.size());
        final List<Geometry> mergedPolygons = new ArrayList<>(polygons.stream().filter(p -> p.getArea() > 20).toList());
        final List<Geometry> tooSmallPolygons = polygons.stream().filter(p -> p.getArea() <= 20).toList();

        for (Geometry p : tooSmallPolygons) {
            for (int i = 0; i < mergedPolygons.size(); i++) {
                if (p.touches(mergedPolygons.get(i))) {
                    Geometry g = mergedPolygons.get(i);
                    mergedPolygons.set(i, CascadedPolygonUnion.union(List.of(p, g)));
                    break;
                }
            }
        }
        return mergedPolygons;
    }
}
