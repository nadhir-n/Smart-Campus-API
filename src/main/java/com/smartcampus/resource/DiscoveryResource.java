package com.smartcampus.resource;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.Map;

@Path("")
public class DiscoveryResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Object> getDiscoveryInfo() {
        return Map.of(
            "version", "1.0",
            "contact", "admin@smartcampus.edu",
            "links", Map.of(
                "rooms", "/api/v1/rooms",
                "sensors", "/api/v1/sensors"
            )
        );
    }
}
