package com.smartcampus.resource;

import com.smartcampus.exception.LinkedResourceNotFoundException;
import com.smartcampus.model.Room;
import com.smartcampus.model.Sensor;
import com.smartcampus.store.DataStore;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Path("/sensors")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorResource {

    @GET
    public Response getAllSensors(@QueryParam("type") String type) {
        List<Sensor> result = new ArrayList<>(DataStore.sensors.values());
        if (type != null && !type.isBlank()) {
            result = result.stream()
                    .filter(s -> s.getType() != null && s.getType().equalsIgnoreCase(type))
                    .collect(Collectors.toList());
        }
        return Response.ok(result).build();
    }

    @POST
    public Response createSensor(Sensor sensor, @Context UriInfo uriInfo) {
        if (sensor == null || sensor.getId() == null || sensor.getId().isBlank()) {
            Map<String, Object> err = new HashMap<>();
            err.put("status", 400);
            err.put("error", "Bad Request");
            err.put("message", "Sensor ID is required.");
            return Response.status(Response.Status.BAD_REQUEST).entity(err).build();
        }
        if (DataStore.sensors.containsKey(sensor.getId())) {
            Map<String, Object> err = new HashMap<>();
            err.put("status", 409);
            err.put("error", "Conflict");
            err.put("message", "A sensor with ID '" + sensor.getId() + "' already exists.");
            return Response.status(Response.Status.CONFLICT).entity(err).build();
        }
        if (sensor.getRoomId() == null || sensor.getRoomId().isBlank()) {
            Map<String, Object> err = new HashMap<>();
            err.put("status", 400);
            err.put("error", "Bad Request");
            err.put("message", "roomId is required.");
            return Response.status(Response.Status.BAD_REQUEST).entity(err).build();
        }
        if (!DataStore.rooms.containsKey(sensor.getRoomId())) {
            throw new LinkedResourceNotFoundException(
                    sensor.getRoomId() == null ? "null" : sensor.getRoomId());
        }
        if (sensor.getStatus() == null || sensor.getStatus().isBlank()) {
            sensor.setStatus("ACTIVE");
        }
        DataStore.sensors.put(sensor.getId(), sensor);
        DataStore.rooms.get(sensor.getRoomId()).getSensorIds().add(sensor.getId());
        DataStore.readings.put(sensor.getId(), new ArrayList<>());

        Map<String, Object> resp = new HashMap<>();
        resp.put("message", "Sensor registered successfully.");
        resp.put("sensor", sensor);
        return Response.status(Response.Status.CREATED).entity(resp).build();
    }

    @GET
    @Path("/{sensorId}")
    public Response getSensor(@PathParam("sensorId") String sensorId) {
        Sensor sensor = DataStore.sensors.get(sensorId);
        if (sensor == null) {
            Map<String, Object> err = new HashMap<>();
            err.put("status", 404);
            err.put("error", "Not Found");
            err.put("message", "Sensor '" + sensorId + "' does not exist.");
            return Response.status(Response.Status.NOT_FOUND).entity(err).build();
        }
        return Response.ok(sensor).build();
    }

    @DELETE
    @Path("/{sensorId}")
    public Response deleteSensor(@PathParam("sensorId") String sensorId) {
        Sensor sensor = DataStore.sensors.get(sensorId);
        if (sensor == null) {
            Map<String, Object> err = new HashMap<>();
            err.put("status", 404);
            err.put("error", "Not Found");
            err.put("message", "Sensor '" + sensorId + "' does not exist.");
            return Response.status(Response.Status.NOT_FOUND).entity(err).build();
        }
        String roomId = sensor.getRoomId();
        if (roomId != null && DataStore.rooms.containsKey(roomId)) {
            DataStore.rooms.get(roomId).getSensorIds().remove(sensorId);
        }
        DataStore.sensors.remove(sensorId);
        DataStore.readings.remove(sensorId);

        Map<String, Object> resp = new HashMap<>();
        resp.put("message", "Sensor '" + sensorId + "' has been removed.");
        return Response.ok(resp).build();
    }

    @Path("/{sensorId}/readings")
    public SensorReadingResource getReadingsResource(@PathParam("sensorId") String sensorId) {
        return new SensorReadingResource(sensorId);
    }
}