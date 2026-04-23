package com.smartcampus.resource;

import com.smartcampus.exception.SensorUnavailableException;
import com.smartcampus.model.Sensor;
import com.smartcampus.model.SensorReading;
import com.smartcampus.store.DataStore;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.UUID;

public class SensorReadingResource {

    private final String sensorId;

    public SensorReadingResource(String sensorId) {
        this.sensorId = sensorId;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getReadings() {
        Sensor sensor = DataStore.sensors.get(sensorId);
        if (sensor == null) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        List<SensorReading> sensorReadings = DataStore.readings.get(sensorId);
        return Response.ok(sensorReadings).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addReading(SensorReading input) {
        if (input == null) {
            throw new WebApplicationException("Reading value is required", Response.Status.BAD_REQUEST);
        }
        Sensor sensor = DataStore.sensors.get(sensorId);
        if (sensor == null) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        if ("MAINTENANCE".equals(sensor.getStatus())) {
            throw new SensorUnavailableException();
        }
        SensorReading reading = new SensorReading();
        reading.setId(UUID.randomUUID().toString());
        reading.setTimestamp(System.currentTimeMillis());
        reading.setValue(input.getValue());

        sensor.setCurrentValue(reading.getValue());
        DataStore.readings.get(sensorId).add(reading);

        return Response.status(Response.Status.CREATED).entity(reading).build();
    }
}