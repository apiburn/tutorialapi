package com.tutorialapi.rest.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ErrorResponseTest {
    @Test
    public void testConstructor() {
        ErrorResponse errorResponse = new ErrorResponse(401, "Unauthorized");
        Assertions.assertEquals(401, errorResponse.getStatus());
        Assertions.assertEquals("Unauthorized", errorResponse.getMessage());
    }

    @Test
    public void testJsonSerialization() throws JsonProcessingException {
        ErrorResponse errorResponse = new ErrorResponse(401, "Unauthorized");
        String json = new ObjectMapper().writeValueAsString(errorResponse);
        String expected = "{\"status\":401,\"message\":\"Unauthorized\"}";
        Assertions.assertEquals(expected, json);
    }

    @Test
    public void testJsonDeserialization() throws JsonProcessingException {
        String json = "{\"status\":401,\"message\":\"Unauthorized\"}";
        ErrorResponse errorResponse = new ObjectMapper().readValue(json, ErrorResponse.class);
        ErrorResponse expected = new ErrorResponse(401, "Unauthorized");
        Assertions.assertEquals(expected, errorResponse);
    }
}
