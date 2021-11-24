package com.tutorialapi.rest.security;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.ext.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Provider
public class AccessLogFilter implements ContainerRequestFilter {
    private static final Logger LOGGER = LoggerFactory.getLogger("access-log");

    @Override
    public void filter(ContainerRequestContext containerRequestContext) {
        String user = "TODO";
        String method = containerRequestContext.getMethod();
        String path = containerRequestContext.getUriInfo().getAbsolutePath().getPath();

        LOGGER.info("{} => {} {}", user, method, path);
    }
}
