package com.lorbeer.randomland.services.geometry;

import com.lorbeer.randomland.domain.*;
import com.lorbeer.randomland.generator.PopulationGenerator;
import com.lorbeer.randomland.util.Utils;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.jboss.logging.Logger;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.operation.overlay.OverlayOp;
import org.locationtech.jts.operation.union.CascadedPolygonUnion;

import java.util.*;

@ApplicationScoped
public class BuildingGeometryService {
    private static final Logger Log = Logger.getLogger(BuildingGeometryService.class);
    private final Random random = new Random();

    @ConfigProperty(name = "gebaeude.wohnen")
    double percentageWohnen;
    @ConfigProperty(name = "wohnen.zweizimmer")
    double wohnenZweizimmer;
    @ConfigProperty(name = "wohnen.dreizimmer")
    double wohnenDreizimmer;
    @ConfigProperty(name = "wohnen.vierzimmer")
    double wohnenVierzimmer;
    @ConfigProperty(name = "wohnen.fuenfzimmer")
    double wohnenFuenfzimmer;
    @ConfigProperty(name = "wohnen.sechszimmer")
    double wohnenSechszimmer;
    @ConfigProperty(name = "wohnen.siebenzimmer")
    double wohnenSiebenimmer;
    @ConfigProperty(name = "wohnen.einzimmer")
    double wohnenEinzimmer;

    @ConfigProperty(name = "alter.vorkriegsbau")
    double vorkriegsbau;
    @ConfigProperty(name = "alter.nachkriegsbau")
    double nachkriegsbau;
    @ConfigProperty(name = "alter.ersteWaermeschutzverordnung")
    double ersteWaermeschutzverordnung;
    @ConfigProperty(name = "alter.zweiteWaermeschutzverordnung")
    double zweiteWaermeschutzverordnung;
    @ConfigProperty(name = "alter.dritteWermeschutzverordnung")
    double dritteWermeschutzverordnung;
    @ConfigProperty(name = "alter.ersteEnergieeinsparverordnung")
    double ersteEnergieeinsparverordnung;
    @ConfigProperty(name = "alter.zweiteEnergieeinsparverordnung")
    double zweiteEnergieeinsparverordnung;

    @ConfigProperty(name = "power.erdgas")
    double erdgas;
    @ConfigProperty(name = "power.fernwaerme")
    double fernwaerme;
    @ConfigProperty(name = "power.heizoel")
    double heizoel;

    @Inject
    PopulationGenerator populationGenerator;


    private final GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();

    public List<Gebaeude> createPolygonsForBuildings(List<Flurstueck> flurstuecks) {
        Log.info("start " + flurstuecks.size());
        final List<Geometry> filterFlurstuecke = new ArrayList<>(flurstuecks.parallelStream().filter(n -> n.usage().equals(NutungsartFlurstueck.GEBAUDE)).map(Flurstueck::shape).toList());
        final List<Polygon> gebaeudePolygons = new ArrayList<>();
        Log.info("filter " + filterFlurstuecke.size());

        int count = 0;
        for (Polygon pol : cleanUp(filterFlurstuecke)) {
            if (pol.getArea() < 20000) {
                if (pol.isValid()) {
                    final List<Polygon> squares = getSquaresFromPol(pol, 1);
                    if (!squares.isEmpty()) {
                        final Geometry geo = CascadedPolygonUnion.union(squares);
                        if (geo instanceof MultiPolygon) {
                            for (int i = 0; i < geo.getNumGeometries(); i++) {
                                gebaeudePolygons.add((Polygon) geo.getGeometryN(i));
                            }
                        } else {
                            gebaeudePolygons.add((Polygon) geo);
                        }
                    }
                } else {
                    Log.info("invald ");
                }
            } else {
                count++;
            }
        }

        final List<Gebaeude> gebaeude = new ArrayList<>(createGebaeude(gebaeudePolygons));
        Log.info("skipped " + count);
        return gebaeude;
    }

    private List<Gebaeude> createGebaeude(List<Polygon> polygons) {
        Log.info("createGebäude");
        final Map<String, List<Polygon>> distribution = new HashMap<>();
        distribution.putIfAbsent("wohnen", new ArrayList<>());
        distribution.putIfAbsent("nichtwohngebaeude", new ArrayList<>());
        final List<Gebaeude> gebaeude = new ArrayList<>();
        int allBuildings = polygons.size();
        Collections.shuffle(polygons);

        for (Polygon pol : polygons) {
            if (distribution.get("wohnen").size() < allBuildings * percentageWohnen && pol.getArea() > 20) {
                distribution.get("wohnen").add(pol);
            } else {
                distribution.get("nichtwohngebaeude").add(pol);
            }
        }

        gebaeude.addAll(verteilungWohngebaeude(distribution.get("wohnen")));
        gebaeude.addAll(verteilungNichtWohngebaeude(distribution.get("nichtwohngebaeude")));

        return gebaeude;
    }

    private List<Gebaeude> verteilungWohngebaeude(List<Polygon> polygons) {
        final List<Gebaeude> gebaeude = verteilungZimmer(polygons);
        verteilungEnergie(gebaeude);
        verteilungBaujahr(gebaeude);
        getHeightRegardingLocation(gebaeude);
        return gebaeude;
    }

    private List<Gebaeude> verteilungNichtWohngebaeude(List<Polygon> polygons) {
        final List<Gebaeude> gebaeude = new ArrayList<>();
        for (Polygon pol : polygons) {
            final Gebaeude geb = new Gebaeude();
            geb.setShape(pol);
            geb.setDach(getRandomDach());
            geb.setWohneinheiten(0);
            if (pol.getArea() < 20 && random.nextInt(10) < 2) {
                geb.setUsage(NutzungsartGebaeude.UNBEHEIZT);
            } else if (pol.getArea() < 300 && pol.getArea() < 600 && random.nextInt(10) < 1) {
                geb.setUsage(NutzungsartGebaeude.HALLE);
                geb.setHeight(6.0);
                geb.setDach(Dach.FLACHDACH);
            } else {
                geb.setUsage(NutzungsartGebaeude.GEWERBE);
            }
            gebaeude.add(geb);
        }

        verteilungBaujahr(gebaeude);
        verteilungEnergie(gebaeude);
        getHeightRegardingLocation(gebaeude);

        return gebaeude;
    }

    private void verteilungEnergie(List<Gebaeude> gebaeudeList) {
        final int size = gebaeudeList.size();
        int erdgasSize = 0;
        int fernwaermeSize = 0;
        int heizoelSize = 0;
        for (Gebaeude g : gebaeudeList) {
            if (erdgasSize < size * erdgas) {
                g.setEnergy(Energietraeger.ERDGAS);
                erdgasSize++;
            } else if (fernwaermeSize < size * fernwaerme) {
                g.setEnergy(Energietraeger.FERNWAERME);
                fernwaermeSize++;
            } else if (heizoelSize < size * heizoel) {
                g.setEnergy(Energietraeger.HEIZOEL);
                heizoelSize++;
            }
        }
    }

    private void verteilungBaujahr(List<Gebaeude> gebaeudeList) {
        final int size = gebaeudeList.size();
        int vorkriegsbauSize = 0;
        int nachkriegsbauSize = 0;
        int ersteWaermeschutzverordnungSize = 0;
        int zweiteWaermeschutzverordnungSize = 0;
        int dritteWermeschutzverordnungSize = 0;
        int ersteEnergieeinsparverordnungSize = 0;
        int zweiteEnergieeinsparverordnungSize = 0;
        for (Gebaeude g : gebaeudeList) {
            if (vorkriegsbauSize < size * vorkriegsbau) {
                g.setBaujahrklasse(Baujahrklassen.VORKRIEGSBAU);
                vorkriegsbauSize++;
            } else if (nachkriegsbauSize < size * nachkriegsbau) {
                g.setBaujahrklasse(Baujahrklassen.NACHKRIEGSBAU);
                nachkriegsbauSize++;
            } else if (ersteWaermeschutzverordnungSize < size * ersteWaermeschutzverordnung) {
                g.setBaujahrklasse(Baujahrklassen.ERSTE_WAERMESCHUTZVERORDNUNG);
                ersteWaermeschutzverordnungSize++;
            } else if (zweiteWaermeschutzverordnungSize < size * zweiteWaermeschutzverordnung) {
                g.setBaujahrklasse(Baujahrklassen.ZWEITE_WAERMESCHUTZVERORDNUNG);
                zweiteWaermeschutzverordnungSize++;
            } else if (dritteWermeschutzverordnungSize < size * dritteWermeschutzverordnung) {
                g.setBaujahrklasse(Baujahrklassen.DRITTE_WAERMESCHUTZVERORDNUNG);
                dritteWermeschutzverordnungSize++;
            } else if (ersteEnergieeinsparverordnungSize < size * ersteEnergieeinsparverordnung) {
                g.setBaujahrklasse(Baujahrklassen.ERSTE_EINSPARVERORDNUNG);
                ersteEnergieeinsparverordnungSize++;
            } else if (zweiteEnergieeinsparverordnungSize < size * zweiteEnergieeinsparverordnung) {
                g.setBaujahrklasse(Baujahrklassen.ZWEITE_EINSPARVERORDNUNG);
                zweiteEnergieeinsparverordnungSize++;
            }
        }

    }


    private List<Gebaeude> verteilungZimmer(List<Polygon> distribution) {
        final Map<String, List<Gebaeude>> verteilungGebaeude = new HashMap<>();
        final List<Polygon> wohnGebaeude = distribution.stream().sorted(Comparator.comparingDouble(Polygon::getArea)).toList();
        verteilungGebaeude.putIfAbsent("einzimmer", new ArrayList<>());
        verteilungGebaeude.putIfAbsent("zweizimmer", new ArrayList<>());
        verteilungGebaeude.putIfAbsent("dreizimmer", new ArrayList<>());
        verteilungGebaeude.putIfAbsent("vierzimmer", new ArrayList<>());
        verteilungGebaeude.putIfAbsent("fuenfzimmer", new ArrayList<>());
        verteilungGebaeude.putIfAbsent("sechszimmer", new ArrayList<>());
        verteilungGebaeude.putIfAbsent("siebenzimmer", new ArrayList<>());
        int gebaeudeSize = wohnGebaeude.size();

        for (Polygon pol : wohnGebaeude) {
            final Gebaeude gebaeude = new Gebaeude();
            gebaeude.setUsage(NutzungsartGebaeude.WOHNEN);
            gebaeude.setShape(pol);
            gebaeude.setDach(getRandomDach());
            if (verteilungGebaeude.get("einzimmer").size() < gebaeudeSize * wohnenEinzimmer) {
                gebaeude.setWohneinheiten(1);
                verteilungGebaeude.get("einzimmer").add(gebaeude);
            } else if (verteilungGebaeude.get("zweizimmer").size() < gebaeudeSize * wohnenZweizimmer) {
                gebaeude.setWohneinheiten(2);
                verteilungGebaeude.get("zweizimmer").add(gebaeude);
            } else if (verteilungGebaeude.get("dreizimmer").size() < gebaeudeSize * wohnenDreizimmer) {
                gebaeude.setWohneinheiten(3);
                verteilungGebaeude.get("dreizimmer").add(gebaeude);
            } else if (verteilungGebaeude.get("vierzimmer").size() < gebaeudeSize * wohnenVierzimmer) {
                gebaeude.setWohneinheiten(4);
                verteilungGebaeude.get("vierzimmer").add(gebaeude);
            } else if (verteilungGebaeude.get("fuenfzimmer").size() < gebaeudeSize * wohnenFuenfzimmer) {
                gebaeude.setWohneinheiten(5);
                verteilungGebaeude.get("fuenfzimmer").add(gebaeude);
            } else if (verteilungGebaeude.get("sechszimmer").size() < gebaeudeSize * wohnenSechszimmer) {
                gebaeude.setWohneinheiten(6);
                verteilungGebaeude.get("sechszimmer").add(gebaeude);
            } else if (verteilungGebaeude.get("siebenzimmer").size() < gebaeudeSize * wohnenSiebenimmer) {
                gebaeude.setWohneinheiten(7);
                verteilungGebaeude.get("siebenzimmer").add(gebaeude);
            }
        }
        final List<Gebaeude> returnList = new ArrayList<>();
        verteilungGebaeude.values().forEach(returnList::addAll);
        verteilungBaujahr(returnList);
        verteilungEnergie(returnList);
        return returnList;
    }

    private void getHeightRegardingLocation(List<Gebaeude> gebaeude) {
        //festgelegt an München -> nichts darf höher als Frauenkirche -> 100
        final int maxHeight = 99;
        // https://stadt.muenchen.de/dam/jcr:a3936be0-d0ce-44f9-b4ef-46562c7efcfb/grz_gfz_2017_download.pdf
        final double minHeight = 3;
        for (Gebaeude geb : gebaeude) {
            if (!geb.usage().equals(NutzungsartGebaeude.HALLE)) {
                final Point point = geb.shape().getCentroid();
                float population = populationGenerator.getPopulationAtPosition(point.getX(), point.getY());
                geb.setHeight(Math.max(maxHeight * population, minHeight));
            }
        }
    }

    private Dach getRandomDach() {
        return Dach.values()[random.nextInt(Dach.values().length)];
    }


    private List<Polygon> cleanUp(List<Geometry> polygons) {
        Log.info("clean up start");
        final List<Polygon> cleanedPolygons = new ArrayList<>();

        for (Geometry g : polygons) {
            if (!g.isEmpty()) {
                if (g instanceof MultiPolygon) {
                    cleanedPolygons.addAll(Utils.iterateThroughMultiPolygon((MultiPolygon) g));
                } else if (g instanceof Polygon) {
                    cleanedPolygons.add((Polygon) g);
                } else {
                    Log.info("cleanup type not added" + g.getGeometryType());
                }
            }
        }
        return cleanedPolygons;

    }

    private List<Polygon> getPolygonsFromLineStrings(List<LineString> xAxis, List<LineString> yAxis) {

        final HashSet<Polygon> polygons = new HashSet<>();

        for (int i = 1; i < xAxis.size(); i++) {
            final LineString xline = xAxis.get(i - 1);
            final LineString xline2 = xAxis.get(i);
            for (int j = 1; j < yAxis.size(); j++) {
                final LineString yline = yAxis.get(j - 1);
                final LineString yline2 = yAxis.get(j);

                if (xline.intersects(yline) && xline.intersects(yline2) && xline2.intersects(yline) && xline2.intersects(yline2)) {
                    final Point x1 = (Point) xline.intersection(yline);
                    final Point x2 = (Point) xline2.intersection(yline);
                    final Point y1 = (Point) xline.intersection(yline2);
                    final Point y2 = (Point) xline2.intersection(yline2);

                    Polygon pol = geometryFactory.createPolygon(new Coordinate[]{x1.getCoordinate(), x2.getCoordinate(), y2.getCoordinate(), y1.getCoordinate(), x1.getCoordinate()});

                    polygons.add(pol);
                }
            }
        }
        return polygons.stream().toList();
    }

    private List<Polygon> getSquaresFromPol(Polygon rectangularPolygon, double sideLength) {
        rectangularPolygon.buffer(0);
        Coordinate[] rectCoords = rectangularPolygon.getCoordinates();
        double[] yList = new double[rectCoords.length];
        double[] xList = new double[rectCoords.length];
        for (int i = 0; i < rectCoords.length; i++) {
            yList[i] = rectCoords[i].y;
            xList[i] = rectCoords[i].x;
        }
        double y1 = getMin(yList);
        double y2 = getMax(yList);
        double x1 = getMin(xList);
        double x2 = getMax(xList);
        double width = x2 - x1;
        double height = y2 - y1;

        int xcells = (int) Math.round(width / sideLength);
        int ycells = (int) Math.round(height / sideLength);

        double[] yIndices = getLinearSpacing(y1, y2, ycells + 1);
        double[] xIndices = getLinearSpacing(x1, x2, xcells + 1);
        List<LineString> horizontalSplitters = new ArrayList<>();
        for (double x : xIndices) {
            horizontalSplitters.add(createLineString(x, yIndices[0], x, yIndices[yIndices.length - 1]));
        }
        List<LineString> verticalSplitters = new ArrayList<>();
        for (double y : yIndices) {
            verticalSplitters.add(createLineString(xIndices[0], y, xIndices[xIndices.length - 1], y));
        }
        final List<Geometry> resultX = new ArrayList<>();
        final List<Geometry> resultY = new ArrayList<>();
        for (LineString splitter : verticalSplitters) {
            try {
                resultY.add(OverlayOp.overlayOp(rectangularPolygon, splitter, OverlayOp.INTERSECTION));
            } catch (TopologyException e) {
                Log.error(e.getMessage());
            }
        }
        for (LineString splitter : horizontalSplitters) {
            try {
                resultX.add(OverlayOp.overlayOp(rectangularPolygon, splitter, OverlayOp.INTERSECTION));
            } catch (TopologyException e) {
                Log.error(e.getMessage());
            }
        }

        final List<LineString> filteredX = filterLineStrings(resultX);
        final List<LineString> filteredY = filterLineStrings(resultY);


        return getPolygonsFromLineStrings(filteredX, filteredY);
    }

    private List<LineString> filterLineStrings(List<Geometry> lineStrings) {
        final List<LineString> filteredLineStrings = new ArrayList<>();
        for (Geometry g : lineStrings) {
            if (g instanceof MultiLineString multiLineString) {
                for (int i = 0; i < multiLineString.getNumGeometries(); i++) {
                    filteredLineStrings.add((LineString) multiLineString.getGeometryN(i));
                }
            } else if (g instanceof LineString lineString) {
                filteredLineStrings.add(lineString);
            }
        }
        return filteredLineStrings;
    }

    private double getMin(double[] values) {
        double min = Double.MAX_VALUE;
        for (double value : values) {
            if (value < min) {
                min = value;
            }
        }
        return min;
    }

    private double getMax(double[] values) {
        double max = Double.MIN_VALUE;
        for (double value : values) {
            if (value > max) {
                max = value;
            }
        }
        return max;
    }

    private double[] getLinearSpacing(double start, double end, int numPoints) {
        double[] result = new double[numPoints];
        double step = (end - start) / (numPoints - 1);
        for (int i = 0; i < numPoints; i++) {
            result[i] = start + i * step;
        }
        return result;
    }

    private LineString createLineString(double x1, double y1, double x2, double y2) {
        Coordinate[] coordinates = {new Coordinate(x1, y1), new Coordinate(x2, y2)};
        return geometryFactory.createLineString(coordinates);
    }
}



