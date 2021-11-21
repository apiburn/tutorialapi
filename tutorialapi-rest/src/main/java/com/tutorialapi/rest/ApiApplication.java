package com.tutorialapi.rest;

import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApiApplication extends ResourceConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(ApiApplication.class);

    public ApiApplication() {
        packages(ApiApplication.class.getPackageName());

        register(new AbstractBinder() {
            @Override
            protected void configure() {
                LOGGER.info("Configuring binder");
            }
        });
    }
}
