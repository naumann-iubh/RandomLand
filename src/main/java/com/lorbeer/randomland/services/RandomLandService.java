package com.lorbeer.randomland.services;

import com.lorbeer.randomland.domain.Baublock;
import com.lorbeer.randomland.domain.Flurstueck;
import com.lorbeer.randomland.domain.Gebaeude;
import com.lorbeer.randomland.domain.Status;
import com.lorbeer.randomland.exception.CreatePackageException;
import com.lorbeer.randomland.exception.NodeTreeException;
import com.lorbeer.randomland.exception.RoadGeometryException;
import com.lorbeer.randomland.generator.PopulationGenerator;
import com.lorbeer.randomland.generator.RoadGenerator;
import com.lorbeer.randomland.generator.domain.NodeTree;
import com.lorbeer.randomland.services.geometry.BaubloeckeService;
import com.lorbeer.randomland.services.geometry.BuildingGeometryService;
import com.lorbeer.randomland.services.geometry.FlurstueckUndNutzungsService;
import com.lorbeer.randomland.services.geometry.RoadGeometryService;
import com.lorbeer.randomland.services.geopackage.CreateGeoPackage;
import com.lorbeer.randomland.util.CSVExport;
import com.lorbeer.randomland.util.Utils;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.io.File;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class RandomLandService {
    private static final Logger Log = Logger.getLogger(RandomLandService.class);

    @ConfigProperty(name = "settings.debug")
    boolean debug;

    @Inject
    PopulationGenerator populationGenerator;

    @Inject
    RoadGenerator roadGenerator;

    @Inject
    CreateGeoPackage createGeoPackage;

    @Inject
    RoadGeometryService roadGeometryService;

    @Inject
    BuildingGeometryService geometryService;

    @Inject
    BaubloeckeService baubloeckeService;

    @Inject
    FlurstueckUndNutzungsService flurstueckUndNutzungsService;

    @Inject
    CSVExport csvExport;

    final Map<String, Status> status = new ConcurrentHashMap<>();
    final Map<String, NodeTree> nodeTrees = new ConcurrentHashMap<>();

    public void generate(Optional<Long> seed, String id, String exportType) {
        try {
            status.putIfAbsent(id, Status.RUNNING);
            final LocalDateTime now = LocalDateTime.now();
            Log.info("Generating random land...");
            populationGenerator.generatePopulationHeatMap(seed, id);
            Log.info("Population heat map generated.");
            Log.info("Start road generation.");
            roadGenerator.startGeneration(id);
            nodeTrees.put(id, roadGenerator.getNodeTree());
            Log.info("road  generated.");
            Log.info("road geometry started");
            final Flurstueck roads = roadGeometryService.createRoadGeometry(roadGenerator.getNodeTree(), id);
            Log.info("Road geometry created.");
            Log.info("Start baublock geometry...");
            final Baublock baublock = baubloeckeService.createBaubloecke(roads.shape());
            Log.info("Baublock created.");
            Log.info("Start create FlurstueckUndNutzung...");
            final List<Flurstueck> flurstueckUndNutzung = flurstueckUndNutzungsService.createFlurstueckundNutzung(baublock.shape());
            Log.info("FlurstueckUndNutzung created.");
            Log.info("Start create Gebaude...");
            final List<Gebaeude> gebaeude = geometryService.createPolygonsForBuildings(flurstueckUndNutzung);
            Log.info("Gebaude created.");


            switch (exportType) {
                case "gpkg":
                    createGeoPackage.createPackageRoad(roads, now, id);
                    createGeoPackage.createPackageBaublock(baublock, now, id);
                    createGeoPackage.createPackageFlurstueck(flurstueckUndNutzung, now, id);
                    createGeoPackage.createPackageGebaeude(gebaeude, now, id);

                    break;
                case "csv":
                    csvExport.csvExportRoad(roads);
                    csvExport.csvExportBaublock(baublock);
                    csvExport.csvExportFlurstueck(flurstueckUndNutzung);
                    csvExport.csvExportGebaeude(gebaeude);
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported export type: " + exportType);

            }
            if (!debug) {
                Utils.zipFiles(id, exportType);
            }
            status.replace(id, Status.DONE);
        } catch (RoadGeometryException e) {
            Log.error(e);
            status.replace(e.getId(), Status.ERROR);
        } catch (NodeTreeException e) {
            Log.error(e);
            status.replace(e.getId(), Status.ERROR);
        } catch (CreatePackageException e) {
            Log.error(e);
            status.replace(e.getId(), Status.ERROR);
        }

    }

    public Status getStatus(String id) {
        return status.getOrDefault(id, Status.NOT_FOUND);
    }

    public Optional<NodeTree> getNodetree(String id) {
        if (nodeTrees.containsKey(id)) {
            return Optional.of(nodeTrees.get(id));
        }
        return Optional.empty();
    }

    public File getGeopackage(String id) {
        return createGeoPackage.getGeopackage(id);
    }

    public File getCSV(String id) {
        return csvExport.getCSV(id);
    }


}
