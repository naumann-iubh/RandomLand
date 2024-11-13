package com.lorbeer.randomland.util;

import com.lorbeer.randomland.exception.CreatePackageException;
import org.jboss.logging.Logger;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineSegment;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

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

    public static void zipFiles(String id, String path) throws CreatePackageException {
        final long dayInMillis = 86400000;
        try {
            final List<File> files = Stream.of(new File(path).listFiles()).filter(f -> f.getAbsoluteFile().toString().endsWith("." + path)).toList();
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
}
