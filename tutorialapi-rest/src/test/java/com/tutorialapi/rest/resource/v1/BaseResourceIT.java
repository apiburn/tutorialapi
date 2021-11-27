package com.tutorialapi.rest.resource.v1;

import com.tutorialapi.rest.exception.ErrorResponse;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.jupiter.api.Assertions;

import java.util.logging.LogManager;

public abstract class BaseResourceIT extends JerseyTest {
    static {
        LogManager.getLogManager().reset();
    }

    protected void verifyErrorResponse(Response response, int expectedStatus, String expectedErrorMessage) {
        Assertions.assertEquals(expectedStatus, response.getStatus());
        Assertions.assertEquals(MediaType.APPLICATION_JSON_TYPE, response.getMediaType());

        ErrorResponse errorResponse = response.readEntity(ErrorResponse.class);
        Assertions.assertEquals(expectedStatus, errorResponse.getStatus());
        Assertions.assertEquals(expectedErrorMessage, errorResponse.getMessage());

        verifyCorsHeaders(response);
    }

    protected void verifyCorsHeaders(Response response) {
        Assertions.assertEquals("*", response.getHeaderString("Access-Control-Allow-Origin"));
        Assertions.assertEquals("DELETE, HEAD, GET, OPTIONS, PATCH, POST, PUT",
                response.getHeaderString("Access-Control-Allow-Methods"));
    }
}
