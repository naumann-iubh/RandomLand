package com.lorbeer.randomland.services.geometry;

import com.lorbeer.randomland.domain.Flurstueck;
import com.lorbeer.randomland.domain.NutungsartFlurstueck;
import com.lorbeer.randomland.exception.RoadGeometryException;
import com.lorbeer.randomland.generator.domain.Node;
import com.lorbeer.randomland.generator.domain.NodeTree;
import com.lorbeer.randomland.generator.domain.RoadType;
import com.lorbeer.randomland.util.Utils;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.jboss.logging.Logger;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.operation.union.CascadedPolygonUnion;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@ApplicationScoped
public class RoadGeometryService {

    private static final Logger Log = Logger.getLogger(RoadGeometryService.class);
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(RoadGeometryService.class);

    private final GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();

    @ConfigProperty(name = "render.highwayThickness", defaultValue = "4")
    Integer highwayThickness;

    @ConfigProperty(name = "render.streetThickness", defaultValue = "2")
    Integer streetThickness;

    public Flurstueck createRoadGeometry(NodeTree nodeTree, String id) throws RoadGeometryException {
        Log.info("size " + nodeTree.getNodes().size());
        final List<Map<Node, Set<Node>>> splittedMap = split(nodeTree.getNodeEdges());

        Log.info("splittedMap " + splittedMap.size());

        final List<CompletableFuture<Geometry>> futures = new ArrayList<>();
        splittedMap.forEach(map -> {
            CompletableFuture<Geometry> future = CompletableFuture.supplyAsync(() -> createRoadGeometry(map));
            futures.add(future);
        });

        final CompletableFuture<Void> completableFuture = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));

        try {
            completableFuture.get();
        } catch (InterruptedException | ExecutionException e) {
            Log.error("Something went wrong", e);
            throw new RoadGeometryException(e.getMessage(), id);
        }

        Log.info("about to join");
        final Geometry geom = geometryFactory.buildGeometry(futures.stream().map(CompletableFuture::join).collect(Collectors.toList())).union();
        Log.info("joined");
        return new Flurstueck(geom, NutungsartFlurstueck.STRASSE);
    }

    private Geometry createRoadGeometry(Map<Node, Set<Node>> nodeEdges) {
        Log.info("size " + nodeEdges.size());
        final List<Geometry> streets = new ArrayList<>();
        for (Map.Entry<Node, Set<Node>> entry : nodeEdges.entrySet()) {
            final Node node = entry.getKey();

            for (Node neighbor : entry.getValue()) {
                final LineString road = geometryFactory.createLineString(new Coordinate[]{node.position().toCoordinate(), neighbor.position().toCoordinate()});
                Geometry g;
                if (node.roadType().equals(RoadType.HIGHWAY)) {
                    g = road.buffer(highwayThickness);
                } else {
                    g = road.buffer(streetThickness);
                }
                if (!Utils.isDuplicateByGeometry(streets, g)) {
                    streets.add(g);
                }
            }
        }
        Log.info("streets " + streets.size());
        final Geometry geom = CascadedPolygonUnion.union(streets);
        Log.info("filteredbyGeo " + geom.getNumGeometries());

        return geom;
    }

    private List<Map<Node, Set<Node>>> split(Map<Node, Set<Node>> nodeEdges) {
        List<Map<Node, Set<Node>>> chunkMapList = new ArrayList<>();
        int mapDataSize = nodeEdges.size();
        int processed = 0;
        int chunkSize = mapDataSize / 10;
        if (chunkSize > 7000) {
            chunkSize = 7000;
        }
        while (processed < mapDataSize) {
            int currentChunkSize = (mapDataSize - processed) >= chunkSize ? chunkSize : (mapDataSize - processed);
            chunkMapList.add(nodeEdges.entrySet().stream().skip(processed).limit(currentChunkSize).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
            processed += currentChunkSize;
        }
        return chunkMapList;
    }
}
