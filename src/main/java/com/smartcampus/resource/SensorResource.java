package com.smartcampus.resource;

import com.smartcampus.exception.LinkedResourceNotFoundException;
import com.smartcampus.model.Room;
import com.smartcampus.model.Sensor;
import com.smartcampus.store.DataStore;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

@Path("/sensors")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorResource {

    @GET
    public Collection<Sensor> getAllSensors(@QueryParam("type") String type) {
        Collection<Sensor> sensors = DataStore.sensors.values();
        if (type != null && !type.isEmpty()) {
            return sensors.stream()
                    .filter(s -> s.getType() != null && s.getType().equalsIgnoreCase(type))
                    .collect(Collectors.toList());
        }
        return sensors;
    }

    @POST
    public Response createSensor(Sensor sensor, @Context UriInfo uriInfo) {
        if (sensor == null || sensor.getId() == null || sensor.getId().isBlank()) {
            throw new BadRequestException("Sensor id is required");
        }
        if (sensor.getRoomId() == null || sensor.getRoomId().isBlank()) {
            throw new BadRequestException("roomId is required");
        }
        Room room = DataStore.rooms.get(sensor.getRoomId());
        if (room == null) {
            throw new LinkedResourceNotFoundException();
        }
        DataStore.sensors.put(sensor.getId(), sensor);
        room.getSensorIds().add(sensor.getId());
        DataStore.readings.put(sensor.getId(), new ArrayList<>());
        return Response.created(uriInfo.getAbsolutePathBuilder().path(sensor.getId()).build())
                .entity(sensor)
                .build();
    }

    @GET
    @Path("/{sensorId}")
    public Sensor getSensor(@PathParam("sensorId") String sensorId) {
        Sensor sensor = DataStore.sensors.get(sensorId);
        if (sensor == null) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        return sensor;
    }

    @Path("/{sensorId}/readings")
    public SensorReadingResource getReadingsResource(@PathParam("sensorId") String sensorId) {
        return new SensorReadingResource(sensorId);
    }
}
