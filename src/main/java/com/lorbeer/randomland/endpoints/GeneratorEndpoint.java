package com.lorbeer.randomland.endpoints;

import com.lorbeer.randomland.services.RandomLandService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;

import java.io.File;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Path("/gen")
public class GeneratorEndpoint {

    private static final Logger Log = Logger.getLogger(GeneratorEndpoint.class);

    @Inject
    RandomLandService randomLandService;

    @Path("/generate/{type}")
    @POST
    public Response generate(@QueryParam("seed") Optional<Long> seed, @PathParam("type") String exportType) {
        final UUID uuid = UUID.randomUUID();
        CompletableFuture.runAsync(() -> randomLandService.generate(seed, uuid.toString(), exportType));

        return Response.ok(uuid.toString()).build();
    }

    @Path("/status/{id}")
    @GET
    public Response status(@PathParam("id") String id) {
        return Response.ok(randomLandService.getStatus(id)).build();
    }

    @Path("getGpgk/{id}")
    @GET
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getGpgk(@PathParam("id") String id) {
        File file = randomLandService.getGeopackage(id);
        Log.info(file.getName());
        return Response.ok(file, MediaType.APPLICATION_OCTET_STREAM).header("Content-Disposition", "attachment; filename=\"" + file.getName() + "\"").build();
    }

    @Path("getCSV/{id}")
    @GET
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getCSV(@PathParam("id") String id) {
        File file = randomLandService.getCSV(id);
        Log.info(file.getName());
        return Response.ok(file, MediaType.APPLICATION_OCTET_STREAM).header("Content-Disposition", "attachment; filename=\"" + file.getName() + "\"").build();
    }


}
