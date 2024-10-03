package com.lorbeer.randomland.services.geopackage;

import com.lorbeer.randomland.domain.Baublock;
import com.lorbeer.randomland.domain.Flurstueck;
import com.lorbeer.randomland.domain.Gebaeude;
import com.lorbeer.randomland.exception.CreatePackageException;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.api.feature.simple.SimpleFeatureType;
import org.geotools.data.DataUtilities;
import org.geotools.data.collection.ListFeatureCollection;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.SchemaException;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.geopkg.FeatureEntry;
import org.geotools.geopkg.GeoPackage;
import org.jboss.logging.Logger;
import org.locationtech.jts.geom.Polygon;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.FileTime;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@ApplicationScoped
public class CreateGeoPackage {
    private static final Logger Log = Logger.getLogger(CreateGeoPackage.class);

    @ConfigProperty(name = "gpkg.path")
    String path;


    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH:mm:ss");

    public void createPackageRoad(Flurstueck road, LocalDateTime start, String id) throws CreatePackageException {
        try {
            final SimpleFeatureType type = DataUtilities.createType("road", "shape:Geometry");
            final DefaultFeatureCollection featureCollection = new DefaultFeatureCollection("road");
            featureCollection.add(SimpleFeatureBuilder.build(type, new Object[]{road.shape()}, null));

            createGeoPackage(start.format(formatter) + "_road", featureCollection, start);
        } catch (SchemaException | IOException e) {
            throw new CreatePackageException(e.getMessage(), id);
        }
    }


    public void createPackageBaublock(Baublock baubloecke, LocalDateTime start, String id) throws CreatePackageException {
        try {
            final SimpleFeatureType type = DataUtilities.createType("baublöcke", "shape:Geometry");
            List<SimpleFeature> features = new ArrayList<>();
            for (Polygon pol : baubloecke.shape()) {
                features.add(
                        SimpleFeatureBuilder.build(type, new Object[]{pol}, "baublöcke"));
            }

            final SimpleFeatureCollection featureCollection = new ListFeatureCollection(type, features);
            createGeoPackage(start.format(formatter) + "_baublock", featureCollection, start);
        } catch (SchemaException | IOException e) {
            throw new CreatePackageException(e.getMessage(), id);
        }
    }

    public void createPackageFlurstueck(List<Flurstueck> flurstueckUndNutzung, LocalDateTime start, String id) throws
            CreatePackageException {
        try {
            final SimpleFeatureType type = DataUtilities.createType("flurstueck", "shape:Geometry,type:String");
            final List<SimpleFeature> features = new ArrayList<>();
            for (Flurstueck flur : flurstueckUndNutzung) {
                features.add(
                        SimpleFeatureBuilder.build(type, new Object[]{flur.shape(), flur.usage().name()}, null));
            }
            final SimpleFeatureCollection featureCollection = new ListFeatureCollection(type, features);

            createGeoPackage(start.format(formatter) + "_flurstueck", featureCollection, start);
        } catch (SchemaException | IOException e) {
            throw new CreatePackageException(e.getMessage(), id);
        }
    }


    public void createPackageGebaeude(List<Gebaeude> gebaeude, LocalDateTime start, String id) throws
            CreatePackageException {
        try {
            final SimpleFeatureType type = DataUtilities.createType("gebaeude",
                    "shape:Geometry,Volumen:Double,Baujahr:Integer,Nutzungsart:String,Wohneinheiten:Integer,Energieträger:String");
            final List<SimpleFeature> features = new ArrayList<>();
            for (Gebaeude geb : gebaeude) {
                features.add(
                        SimpleFeatureBuilder.build(type, new Object[]{geb.shape(), geb.volume(), geb.toBaujahr(geb.baujahrklasse()), geb.usage().getValue(), geb.wohneinheiten(), geb.energy().getName()}, "gebaeude4"));
            }
            final SimpleFeatureCollection featureCollection = new ListFeatureCollection(type, features);

            createGeoPackage(start.format(formatter) + "_gebaeude", featureCollection, start);
        } catch (SchemaException | IOException e) {
            throw new CreatePackageException(e.getMessage(), id);
        }
    }

    public void zipFiles(String id) throws CreatePackageException {
        final long dayInMillis = 86400000;
        try {
            final List<File> files = Stream.of(new File(path).listFiles()).filter(f -> f.getAbsoluteFile().toString().endsWith(".gpkg")).toList();
            for (File file : files) {
                FileTime fileTime = (FileTime) Files.getAttribute(file.toPath(), "creationTime");
                if (System.currentTimeMillis() - fileTime.toMillis() > dayInMillis) {
                    file.delete();
                }
            }
            final FileOutputStream fos = new FileOutputStream(path + "/" + id + ".zip");
            final ZipOutputStream zip = new ZipOutputStream(fos);

            for (final File file : files) {
                FileInputStream fis = new FileInputStream(file);
                zip.putNextEntry(new ZipEntry(file.getName()));
                byte[] bytes = new byte[1024];
                int length;
                while ((length = fis.read(bytes)) >= 0) {
                    zip.write(bytes, 0, length);
                }
                fis.close();
            }
            zip.close();
            fos.close();
            files.forEach(File::delete);
        } catch (IOException e) {
            throw new CreatePackageException(e.getMessage(), id);
        }

    }

    private void createGeoPackage(String name, SimpleFeatureCollection collection, LocalDateTime start) throws
            IOException {
        final LocalDateTime dateTime = LocalDateTime.now();

        final GeoPackage geopkg = new GeoPackage(new File(path + "/" + name + ".gpkg"));
        geopkg.init();

        final FeatureEntry entry = new FeatureEntry();
        entry.setDescription("RandomLand_" + name);
        geopkg.addCRS(25832);
        geopkg.add(entry, collection);
        geopkg.createSpatialIndex(entry);
        geopkg.close();

        Log.info("GPKG for " + name + " created start: " + start.format(formatter) + " end: " + dateTime.format(formatter));
    }

    public File getGeopackage(String id) {
        return new File(path + "/" + id + ".zip");
    }


//    https://github.com/ngageoint/geopackage-core-java
}