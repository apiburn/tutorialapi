package com.tutorialapi.rest.resource;

import com.tutorialapi.db.ServiceFactory;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static jakarta.ws.rs.core.MediaType.TEXT_PLAIN;

@Path("/test")
public class HelloResource {
    private static final Logger LOGGER = LoggerFactory.getLogger(HelloResource.class);

    //private final ServiceFactory serviceFactory;

    @Inject
    public HelloResource(ServiceFactory serviceFactory) {
        //this.serviceFactory = serviceFactory;
        LOGGER.info("Initializing service factory to: {}", serviceFactory);
    }

    @GET
    @Produces(TEXT_PLAIN)
    public String test() {
        //RapidApiPrincipal principal = (RapidApiPrincipal) ((RapidApiSecurityContext) securityContext).getUserPrincipal();
        //LOGGER.info("Principal: {}", principal);

        //return serviceFactory.getTodoListService().getAll(principal);
        return "Hello";
    }
}
