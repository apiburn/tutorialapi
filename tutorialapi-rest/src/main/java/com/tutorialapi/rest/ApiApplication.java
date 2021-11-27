package com.tutorialapi.rest;

import com.tutorialapi.db.DefaultServiceFactory;
import com.tutorialapi.db.ServiceFactory;
import com.tutorialapi.model.config.ConfigKey;
import com.typesafe.config.Config;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.swagger.v3.jaxrs2.integration.resources.AcceptHeaderOpenApiResource;
import io.swagger.v3.jaxrs2.integration.resources.OpenApiResource;
import jakarta.ws.rs.ApplicationPath;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;

import javax.sql.DataSource;

@ApplicationPath("/api") // Only used by OpenAPI docs
public class ApiApplication extends ResourceConfig {
    public ApiApplication(Config config) {
        this(config, createServiceFactory(config));
    }

    public ApiApplication(Config config, ServiceFactory serviceFactory) {
        packages(ApiApplication.class.getPackageName());

        register(OpenApiResource.class);
        register(AcceptHeaderOpenApiResource.class);

        register(new AbstractBinder() {
            @Override
            protected void configure() {
                bind(config).to(Config.class);
                bind(serviceFactory).to(ServiceFactory.class);
            }
        });
    }

    private static ServiceFactory createServiceFactory(Config config) {
        HikariConfig dbConfig = new HikariConfig();
        dbConfig.setDriverClassName(config.getString(ConfigKey.DB_DRIVER.getKey()));
        dbConfig.setJdbcUrl(config.getString(ConfigKey.DB_URL.getKey()));
        dbConfig.setUsername(config.getString(ConfigKey.DB_USERNAME.getKey()));
        dbConfig.setPassword(config.getString(ConfigKey.DB_PASSWORD.getKey()));
        dbConfig.setAutoCommit(false);

        DataSource dataSource = new HikariDataSource(dbConfig);
        return new DefaultServiceFactory(dataSource);
    }
}
