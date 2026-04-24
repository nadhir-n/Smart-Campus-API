package com.smartcampus.resource;

import com.smartcampus.exception.SensorUnavailableException;
import com.smartcampus.model.Sensor;
import com.smartcampus.model.SensorReading;
import com.smartcampus.store.DataStore;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SensorReadingResource {

    private final String sensorId;

    public SensorReadingResource(String sensorId) {
        this.sensorId = sensorId;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getReadings() {
        List<SensorReading> list = DataStore.readings.getOrDefault(sensorId, new ArrayList<>());
        Map<String, Object> resp = new HashMap<>();
        resp.put("sensorId", sensorId);
        resp.put("count", list.size());
        resp.put("readings", list);
        return Response.ok(resp).build();
    }

    @GET
    @Path("/{readingId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getReading(@PathParam("readingId") String readingId) {
        List<SensorReading> list = DataStore.readings.getOrDefault(sensorId, new ArrayList<>());
        return list.stream()
                .filter(r -> r.getId().equals(readingId))
                .findFirst()
                .map(r -> Response.ok(r).build())
                .orElse(Response.status(Response.Status.NOT_FOUND)
                        .entity(Map.of(
                                "status", 404,
                                "error", "Not Found",
                                "message", "Reading '" + readingId + "' not found for sensor '" + sensorId + "'."
                        )).build());
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addReading(SensorReading reading) {
        Sensor sensor = DataStore.sensors.get(sensorId);

        if (sensor == null) {
            Map<String, Object> err = new HashMap<>();
            err.put("status", 404);
            err.put("error", "Not Found");
            err.put("message", "Sensor '" + sensorId + "' does not exist.");
            return Response.status(Response.Status.NOT_FOUND).entity(err).build();
        }

        if (!"ACTIVE".equalsIgnoreCase(sensor.getStatus())) {
            throw new SensorUnavailableException(sensorId, sensor.getStatus());
        }

        if (reading == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("status", 400, "error", "Bad Request", "message", "Reading body is required."))
                    .build();
        }

        if (reading.getId() == null || reading.getId().isBlank()) {
            reading.setId(UUID.randomUUID().toString());
        }
        if (reading.getTimestamp() == 0) {
            reading.setTimestamp(System.currentTimeMillis());
        }

        DataStore.readings.computeIfAbsent(sensorId, k -> new ArrayList<>()).add(reading);

        sensor.setCurrentValue(reading.getValue());

        Map<String, Object> resp = new HashMap<>();
        resp.put("message", "Reading recorded successfully.");
        resp.put("sensorId", sensorId);
        resp.put("reading", reading);
        resp.put("sensorCurrentValue", sensor.getCurrentValue());
        return Response.status(Response.Status.CREATED).entity(resp).build();
    }
}