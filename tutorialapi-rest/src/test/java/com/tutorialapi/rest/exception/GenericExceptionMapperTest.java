package com.tutorialapi.rest.exception;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static jakarta.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;

public class GenericExceptionMapperTest {
    @Test
    public void test() {
        Exception exception = new Exception("failed");
        Response response = new GenericExceptionMapper().toResponse(exception);

        Assertions.assertEquals(INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
        Assertions.assertEquals(MediaType.APPLICATION_JSON_TYPE, response.getMediaType());
        Assertions.assertEquals(new ErrorResponse(INTERNAL_SERVER_ERROR.getStatusCode(), "failed"), response.getEntity());
    }
}
