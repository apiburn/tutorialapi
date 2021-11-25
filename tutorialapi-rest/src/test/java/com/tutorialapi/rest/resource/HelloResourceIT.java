package com.tutorialapi.rest.resource;

import com.tutorialapi.model.config.ConfigKey;
import com.tutorialapi.model.user.Subscription;
import com.tutorialapi.rest.ApiApplication;
import com.tutorialapi.rest.exception.ErrorResponse;
import com.tutorialapi.rest.security.SecurityHeader;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.sqlite.JDBC;

import java.util.Properties;
import java.util.logging.LogManager;

public class HelloResourceIT extends JerseyTest {
    static {
        LogManager.getLogManager().reset();
    }

    @Override
    protected Application configure() {
        Properties properties = new Properties();
        properties.setProperty(ConfigKey.DB_DRIVER.getKey(), JDBC.class.getName());
        properties.setProperty(ConfigKey.DB_URL.getKey(), "jdbc:sqlite::memory:");
        properties.setProperty(ConfigKey.DB_USERNAME.getKey(), "");
        properties.setProperty(ConfigKey.DB_PASSWORD.getKey(), "");

        Config config = ConfigFactory.parseProperties(properties);
        return new ApiApplication(config);
    }

    @Test
    public void testNoSecurityHeaders() {
        Response response = target("/test").request().get();

        Assertions.assertEquals(401, response.getStatus());
        Assertions.assertEquals(MediaType.APPLICATION_JSON_TYPE, response.getMediaType());

        ErrorResponse errorResponse = response.readEntity(ErrorResponse.class);
        Assertions.assertEquals(401, errorResponse.getStatus());
        Assertions.assertEquals("Missing security header: X-RapidAPI-Proxy-Secret", errorResponse.getMessage());
    }

    @Test
    public void testOnlyProxySecretHeader() {
        Response response = target("/test").request()
                .header(SecurityHeader.RAPID_API_PROXY_SECRET.getHeader(), "proxy-secret")
                .get();

        Assertions.assertEquals(401, response.getStatus());
        Assertions.assertEquals(MediaType.APPLICATION_JSON_TYPE, response.getMediaType());

        ErrorResponse errorResponse = response.readEntity(ErrorResponse.class);
        Assertions.assertEquals(401, errorResponse.getStatus());
        Assertions.assertEquals("Missing security header: X-RapidAPI-User", errorResponse.getMessage());
    }

    @Test
    public void testProxySecretAndUserHeader() {
        Response response = target("/test").request()
                .header(SecurityHeader.RAPID_API_PROXY_SECRET.getHeader(), "proxy-secret")
                .header(SecurityHeader.RAPID_API_USER.getHeader(), "user")
                .get();

        Assertions.assertEquals(401, response.getStatus());
        Assertions.assertEquals(MediaType.APPLICATION_JSON_TYPE, response.getMediaType());

        ErrorResponse errorResponse = response.readEntity(ErrorResponse.class);
        Assertions.assertEquals(401, errorResponse.getStatus());
        Assertions.assertEquals("Missing or invalid security header: X-RapidAPI-Subscription",
                errorResponse.getMessage());
    }

    @Test
    public void testInvalidSubscription() {
        Response response = target("/test").request()
                .header(SecurityHeader.RAPID_API_PROXY_SECRET.getHeader(), "proxy-secret")
                .header(SecurityHeader.RAPID_API_USER.getHeader(), "user")
                .header(SecurityHeader.RAPID_API_SUBSCRIPTION.getHeader(), "invalid")
                .get();

        Assertions.assertEquals(401, response.getStatus());
        Assertions.assertEquals(MediaType.APPLICATION_JSON_TYPE, response.getMediaType());

        ErrorResponse errorResponse = response.readEntity(ErrorResponse.class);
        Assertions.assertEquals(401, errorResponse.getStatus());
        Assertions.assertEquals("Missing or invalid security header: X-RapidAPI-Subscription",
                errorResponse.getMessage());
    }

    @Test
    public void testValidHeaders() {
        Response response = target("/test").request()
                .header(SecurityHeader.RAPID_API_PROXY_SECRET.getHeader(), "proxy-secret")
                .header(SecurityHeader.RAPID_API_USER.getHeader(), "user")
                .header(SecurityHeader.RAPID_API_SUBSCRIPTION.getHeader(), Subscription.BASIC.name())
                .get();

        Assertions.assertEquals(200, response.getStatus());
        Assertions.assertEquals(MediaType.TEXT_PLAIN_TYPE, response.getMediaType());
        Assertions.assertEquals("Hello", response.readEntity(String.class));

        Assertions.assertEquals("*", response.getHeaderString("Access-Control-Allow-Origin"));
        Assertions.assertEquals("DELETE, HEAD, GET, OPTIONS, PATCH, POST, PUT",
                response.getHeaderString("Access-Control-Allow-Methods"));
    }
}
