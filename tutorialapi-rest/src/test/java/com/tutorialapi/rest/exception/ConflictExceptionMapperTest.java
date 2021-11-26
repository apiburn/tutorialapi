package com.tutorialapi.rest.exception;

import com.tutorialapi.db.exception.ConflictException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static jakarta.ws.rs.core.Response.Status.CONFLICT;

public class ConflictExceptionMapperTest {
    @Test
    public void test() {
        ConflictException exception = new ConflictException("failed");
        Response response = new ConflictExceptionMapper().toResponse(exception);

        Assertions.assertEquals(CONFLICT.getStatusCode(), response.getStatus());
        Assertions.assertEquals(MediaType.APPLICATION_JSON_TYPE, response.getMediaType());
        Assertions.assertEquals(new ErrorResponse(CONFLICT.getStatusCode(), "failed"), response.getEntity());
    }
}
