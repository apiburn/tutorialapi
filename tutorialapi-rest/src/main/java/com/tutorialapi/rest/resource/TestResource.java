package com.tutorialapi.rest.resource;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;

import static jakarta.ws.rs.core.MediaType.TEXT_PLAIN;

@Path("/test")
public class TestResource {
    @GET
    @Produces(TEXT_PLAIN)
    public String test() {
        return "Hello";
    }
}
