package com.lorbeer.randomland.endpoints;


import com.lorbeer.randomland.generator.PopulationGenerator;
import com.lorbeer.randomland.generator.domain.NodeTree;
import com.lorbeer.randomland.services.RandomLandService;
import com.lorbeer.randomland.util.RenderCity;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Optional;

@Path("/utility")
public class UtilityEndpoint {

    @Inject
    PopulationGenerator populationGenerator;

    @Inject
    RandomLandService randomLandService;

    @Path("/heatmap/")
    @GET
    @Produces("image/png")
    public Response getHeatmap(@QueryParam("uuid") String uuid) throws IOException {

        final Optional<BufferedImage> image = populationGenerator.getImage(uuid);
        if (image.isPresent()) {
            final ByteArrayOutputStream os = new ByteArrayOutputStream();
            ImageIO.write(image.get(), "png", os);
            return Response.ok(os.toByteArray()).build();
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }


    @Path("/roadmap/")
    @GET
    @Produces("image/png")
    public Response getRoadmap(@QueryParam("uuid") String uuid) throws IOException {

        final Optional<NodeTree> tree = randomLandService.getNodetree(uuid);
        if (tree.isPresent()) {
            final RenderCity city = new RenderCity(tree.get());
            final BufferedImage cityImage = city.render();
            final ByteArrayOutputStream os = new ByteArrayOutputStream();
            ImageIO.write(cityImage, "png", os);
            return Response.ok(os.toByteArray()).build();
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }
}
