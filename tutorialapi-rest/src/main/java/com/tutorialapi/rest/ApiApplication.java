package com.tutorialapi.rest;

import io.swagger.v3.jaxrs2.integration.resources.AcceptHeaderOpenApiResource;
import io.swagger.v3.jaxrs2.integration.resources.OpenApiResource;
import jakarta.ws.rs.ApplicationPath;
import org.glassfish.hk2.api.TypeLiteral;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;

import java.util.function.Supplier;

@ApplicationPath("/api") // Only used by OpenAPI docs
public class ApiApplication extends ResourceConfig {
    public ApiApplication(Supplier<Environment> environmentSupplier) {
        packages(ApiApplication.class.getPackageName());

        register(OpenApiResource.class);
        register(AcceptHeaderOpenApiResource.class);

        register(new AbstractBinder() {
            @Override
            protected void configure() {
                bind(environmentSupplier).to(new TypeLiteral<Supplier<Environment>>() {});
            }
        });
    }
}
