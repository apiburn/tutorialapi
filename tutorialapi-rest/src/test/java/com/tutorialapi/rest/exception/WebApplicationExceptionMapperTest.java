package com.tutorialapi.rest.exception;

import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static jakarta.ws.rs.core.Response.Status.NOT_FOUND;

public class WebApplicationExceptionMapperTest {
    @Test
    public void test() {
        WebApplicationException exception = new NotFoundException("not found");
        Response response = new WebApplicationExceptionMapper().toResponse(exception);

        Assertions.assertEquals(NOT_FOUND.getStatusCode(), response.getStatus());
        Assertions.assertEquals(MediaType.APPLICATION_JSON_TYPE, response.getMediaType());
        Assertions.assertEquals(new ErrorResponse(NOT_FOUND.getStatusCode(), "not found"), response.getEntity());
    }
}
