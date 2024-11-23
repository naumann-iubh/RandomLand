package com.lorbeer.randomland.util;


import com.lorbeer.randomland.domain.Baublock;
import com.lorbeer.randomland.domain.Flurstueck;
import com.lorbeer.randomland.domain.Gebaeude;
import com.opencsv.CSVWriter;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.io.WKTWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class CSVExport {

    private static final Logger Log = Logger.getLogger(CSVExport.class);


    @ConfigProperty(name = "csv.path")
    String path;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH:mm:ss");

    private final WKTWriter writer = new WKTWriter();

    public void csvExportRoad(Flurstueck road) {
        final String wkt = writer.write(road.shape());
        writeCSV(List.of(Flurstueck.HEADER(), new String[]{wkt}), "road");
    }

    public void csvExportBaublock(Baublock baubloecke) {
        final List<String[]> data = new ArrayList<>();
        data.add(Baublock.HEADER());
        for (Polygon pol : baubloecke.shape()) {
            final String wkt = writer.write(pol);
            data.add(new String[]{wkt});
        }
        writeCSV(data, "baublock");
    }

    public void csvExportFlurstueck(List<Flurstueck> flurstuecke) {
        final List<String[]> data = new ArrayList<>();
        data.add(Flurstueck.HEADER());
        for (Flurstueck flurstueck : flurstuecke) {
            final String wkt = writer.write(flurstueck.shape());
            data.add(new String[]{wkt, flurstueck.usage().getName()});
        }
        writeCSV(data, "flurstueck");
    }

    public void csvExportGebaeude(List<Gebaeude> gebaeude) {
        final List<String[]> data = new ArrayList<>();
        data.add(Gebaeude.HEADER());
        for (Gebaeude g : gebaeude) {

            final String wkt = writer.write(g.shape());
            data.add(new String[]{wkt,
                    String.valueOf(g.height()),
                    g.dach().getName(),
                    String.valueOf(g.volume()),
                    g.baujahrklasse().getName(),
                    g.usage().getName(),
                    String.valueOf(g.avgTemp()),
                    String.valueOf(g.normTemp()),
                    g.energy().getName()});
        }
        writeCSV(data, "gebaeude");
    }

    public File getCSV(String id) {
        return new File(path + "/" + id + ".zip");
    }

    private void writeCSV(List<String[]> data, String name) {
        final LocalDateTime dateTime = LocalDateTime.now();

        final File csv = new File(path, "csv_" + name + "_" + dateTime.format(formatter) + ".csv");
        if (!csv.getParentFile().exists()) {
            csv.getParentFile().mkdirs();
        }
        try (final CSVWriter writer = new CSVWriter(new FileWriter(csv))) {
            writer.writeAll(data);
        } catch (IOException e) {
            Log.error("Not able to create csv " + csv.getName(), e);
        }


    }
}
