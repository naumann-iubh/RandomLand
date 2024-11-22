package com.lorbeer.randomland.services.geometry;

import com.lorbeer.randomland.domain.Baublock;
import com.lorbeer.randomland.util.Utils;
import jakarta.enterprise.context.ApplicationScoped;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.jboss.logging.Logger;
import org.locationtech.jts.awt.ShapeReader;
import org.locationtech.jts.awt.ShapeWriter;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;

import java.awt.*;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@ApplicationScoped
public class BaubloeckeService {
    private static final Logger Log = Logger.getLogger(BaubloeckeService.class);
    private final GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();

    public Baublock createBaubloecke(Geometry geometry) {

        final Geometry freeSpaceGeometry = createFreeSpaceGeometry(geometry);
        final MultiPolygon freeSpacePolygons = (MultiPolygon) freeSpaceGeometry;
        final List<Polygon> baublockPolygon = new ArrayList<>();

        for (Geometry polygon : Utils.iterateThroughMultiPolygon(freeSpacePolygons)) {
            if (polygon instanceof MultiPolygon) {
                baublockPolygon.addAll(Utils.iterateThroughMultiPolygon((MultiPolygon) polygon));
            } else if (polygon instanceof Polygon) {
                baublockPolygon.add((Polygon) polygon);
            } else {
                Log.warn("Geometry is not a Polygon nor a MultiPolygon " + polygon.getGeometryType());
            }
        }

        final List<Polygon> orderedList = new ArrayList<>(baublockPolygon.stream().sorted(Comparator.comparingDouble(Polygon::getArea)).toList());
        Log.info("baublockPolygon size " + orderedList.getFirst().getArea());
        orderedList.removeLast();
        Log.info("baublockPolygon size " + orderedList.size());
        return new Baublock(orderedList);
    }


    private Geometry createFreeSpaceGeometry(Geometry roadGeometry) {
        Log.info("roadGeometry " + roadGeometry.getNumGeometries());
        final ShapeWriter writer = new ShapeWriter();
        writer.setRemoveDuplicatePoints(true);
        writer.setDecimation(1);


        final Shape roadNetwork = writer.toShape(roadGeometry);
        final Area roadNetWorkArea = new Area(roadNetwork);

        final Rectangle2D rectangle2D = new Rectangle2D.Double(0, 0, roadNetwork.getBounds2D().getWidth() + 10, roadNetwork.getBounds2D().getHeight() + 10);
        final BufferedImage image1 = new BufferedImage((int) rectangle2D.getWidth(), (int) rectangle2D.getHeight(), BufferedImage.TYPE_INT_RGB);
        final Graphics2D graphics2D1 = image1.createGraphics();

        graphics2D1.draw(rectangle2D);
        final Area clean = new Area(rectangle2D);

        clean.subtract(roadNetWorkArea);

        final ShapeReader reader = new ShapeReader(geometryFactory);
        final Geometry poly = reader.read(clean.getPathIterator(null));
        Log.info("poly size " + poly.getGeometryType());

//        saveImage(List.of(roadNetWorkArea, clean));
        return poly;
    }

    /*
    private void saveImage(List<Shape> images) {
        final LocalDateTime now = LocalDateTime.now();
        int counter = 0;
        for (Shape shape : images) {
            final BufferedImage image = new BufferedImage((int) shape.getBounds2D().getWidth() + 10, (int) shape.getBounds2D().getHeight() + 10, BufferedImage.TYPE_INT_RGB);
            final Graphics2D graphics2D = image.createGraphics();
            graphics2D.draw(shape);
            try {
                ImageIO.write(image, "png", new File("./test_" + counter + "_" + now + ".png"));
                counter++;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }*/
}
