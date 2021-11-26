package com.tutorialapi.rest.exception;

import com.tutorialapi.db.exception.ConflictException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import static jakarta.ws.rs.core.Response.Status.CONFLICT;

@Provider
public class ConflictExceptionMapper implements ExceptionMapper<ConflictException> {
    @Override
    public Response toResponse(ConflictException exception) {
        return Response.status(CONFLICT)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .entity(new ErrorResponse(CONFLICT.getStatusCode(), exception.getMessage()))
                .build();
    }
}
