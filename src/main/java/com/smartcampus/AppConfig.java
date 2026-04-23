package com.smartcampus;

import java.util.HashSet;
import java.util.Set;
import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

@ApplicationPath("/api/v1")
public class AppConfig extends Application {

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> classes = new HashSet<>();
        
        classes.add(com.smartcampus.resource.DiscoveryResource.class);
        classes.add(com.smartcampus.resource.RoomResource.class);
        classes.add(com.smartcampus.resource.SensorResource.class);
        
        classes.add(com.smartcampus.exception.RoomNotEmptyExceptionMapper.class);
        classes.add(com.smartcampus.exception.LinkedResourceNotFoundExceptionMapper.class);
        classes.add(com.smartcampus.exception.SensorUnavailableExceptionMapper.class);
        classes.add(com.smartcampus.exception.GlobalExceptionMapper.class);
        
        classes.add(com.smartcampus.filter.LoggingFilter.class);
        
        return classes;
    }
}