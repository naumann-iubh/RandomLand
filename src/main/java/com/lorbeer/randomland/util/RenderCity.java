package com.lorbeer.randomland.util;

import com.lorbeer.randomland.generator.PopulationGenerator;
import com.lorbeer.randomland.generator.domain.Node;
import com.lorbeer.randomland.generator.domain.NodeTree;
import com.lorbeer.randomland.generator.domain.RoadType;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import org.locationtech.jts.math.Vector2D;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Set;

// this is a simple Generator to show the road-design as .png
public class RenderCity {
    private static final Logger Log = Logger.getLogger(RenderCity.class);

    @ConfigProperty(name = "render.showPoints")
    boolean showPoints;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH:mm:ss");

    @Inject
    PopulationGenerator populationGenerator;

    private final NodeTree nodeTree;


    public RenderCity(NodeTree nodeTree) {
        this.nodeTree = nodeTree;
    }

    public BufferedImage render() {
        Log.info("Rendering City w " + nodeTree.getWidth() + " h " + nodeTree.getHeight());
        BufferedImage image = new BufferedImage(nodeTree.getWidth(), nodeTree.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();

        // background
        g.setColor(new Color(255, 255, 204));
        g.fillRect(0, 0, nodeTree.getWidth(), nodeTree.getHeight());

        // noise
        if (showPoints) {
            for (int x = 0; x < nodeTree.getWidth(); x++) {
                for (int y = 0; y < nodeTree.getHeight(); y++) {
                    double noise = populationGenerator.getPopulationAtPosition(x, y);
                    int pixel = (int) (noise * 255);
                    image.setRGB(x, y, new Color(pixel, pixel, pixel).getRGB());
                }
            }
        }

        // vertices
        g.setColor(Color.BLACK);


        Stroke mainStroke = new BasicStroke(2);
        Stroke minorStroke = new BasicStroke(1);

        // edges
        for (Map.Entry<Node, Set<Node>> e : nodeTree.getNodeEdges().entrySet()) {
            Node node = e.getKey();
            for (Node neighbour : e.getValue()) {
                if (node.roadType().equals(RoadType.HIGHWAY) && neighbour.roadType().equals(RoadType.HIGHWAY)) {
                    g.setColor(Color.DARK_GRAY);
                    g.setStroke(mainStroke);
                } else {
                    g.setColor(Color.gray);
                    g.setStroke(minorStroke);
                }

                Vector2D vp = node.position();
                Vector2D np = neighbour.position();
                g.drawLine((int) vp.getX(), (int) vp.getY(), (int) np.getX(), (int) np.getY());

            }
        }

        return image;
    }


}
