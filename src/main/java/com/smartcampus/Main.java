package com.smartcampus;

import java.io.IOException;
import java.net.URI;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;

public class Main {

    public static final String BASE_URI = "http://localhost:8080/api/v1/";

    public static HttpServer startServer() {
        final ResourceConfig config = new ResourceConfig()
                .packages("com.smartcampus")
                .register(JacksonFeature.class);
        
        return GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), config);
    }

    public static void main(String[] args) throws IOException {
        final HttpServer server = startServer();
        System.out.println("Smart Campus API started at " + BASE_URI);
        System.out.println("Press Enter to stop...");
        System.in.read();
        server.shutdownNow();
    }
}
