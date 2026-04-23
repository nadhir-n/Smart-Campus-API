package com.smartcampus.exception;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.util.Map;

@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Throwable> {
    @Override
    public Response toResponse(Throwable exception) {
        if (exception instanceof WebApplicationException webApplicationException) {
            return webApplicationException.getResponse();
        }
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(Map.of("error", "An unexpected internal server error occurred"))
                .build();
    }
}
