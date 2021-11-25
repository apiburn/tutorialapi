package com.tutorialapi.rest.exception;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Provider
public class WebApplicationExceptionMapper implements ExceptionMapper<WebApplicationException> {
    private static final Logger LOGGER = LoggerFactory.getLogger(WebApplicationExceptionMapper.class);

    @Override
    public Response toResponse(WebApplicationException exception) {
        LOGGER.error("Exception processing request", exception);

        return Response.fromResponse(exception.getResponse())
                .type(MediaType.APPLICATION_JSON_TYPE)
                .entity(new ErrorResponse(exception.getResponse().getStatus(), exception.getMessage()))
                .build();
    }
}
