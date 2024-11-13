package com.lorbeer.randomland.util;


import com.lorbeer.randomland.domain.Baublock;
import com.lorbeer.randomland.domain.Flurstueck;
import com.lorbeer.randomland.domain.Gebaeude;
import com.opencsv.CSVWriter;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import org.locationtech.jts.geom.Polygon;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class CSVExport {

    private static final Logger Log = Logger.getLogger(CSVExport.class);


    @ConfigProperty(name = "csv.path")
    String path;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH:mm:ss");


    public void csvExportRoad(Flurstueck road) {
        final String coordinateList = Arrays.stream(road.shape().getCoordinates()).map(c -> "(" + c.x + ", " + c.y + ")").collect(Collectors.joining(","));
        writeCSV(List.of(Flurstueck.HEADER(), new String[]{coordinateList, road.usage().getName()}), "road");
    }

    public void csvExportBaublock(Baublock baubloecke) {
        final List<String[]> data = new ArrayList<>();
        data.add(Baublock.HEADER());
        for (Polygon pol : baubloecke.shape()) {
            final String coordinateList = Arrays.stream(pol.getCoordinates()).map(c -> "(" + c.x + ", " + c.y + ")").collect(Collectors.joining(","));
            data.add(new String[]{coordinateList});
        }
        writeCSV(data, "baublock");
    }

    public void csvExportFlurstueck(List<Flurstueck> flurstuecke) {
        final List<String[]> data = new ArrayList<>();
        data.add(Flurstueck.HEADER());
        for (Flurstueck flurstueck : flurstuecke) {
            final String coordinateList = Arrays.stream(flurstueck.shape().getCoordinates()).map(c -> "(" + c.x + ", " + c.y + ")").collect(Collectors.joining(","));
            data.add(new String[]{coordinateList, flurstueck.usage().getName()});
        }
        writeCSV(data, "flurstueck");
    }

    public void csvExportGebaeude(List<Gebaeude> gebaeude) {
        final List<String[]> data = new ArrayList<>();
        data.add(Gebaeude.HEADER());
        for (Gebaeude g : gebaeude) {
            final String coordinateList = Arrays.stream(g.shape().getCoordinates()).map(c -> "(" + c.x + ", " + c.y + ")").collect(Collectors.joining(","));
            data.add(new String[]{coordinateList,
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
