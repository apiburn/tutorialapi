package com.tutorialapi.rest.security;

import com.tutorialapi.db.ServiceFactory;
import com.tutorialapi.db.service.ApiKeyService;
import com.tutorialapi.model.config.ConfigKey;
import com.tutorialapi.model.user.ApiKey;
import com.tutorialapi.model.user.RapidApiPrincipal;
import com.tutorialapi.model.user.Subscription;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.UriInfo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import java.net.URI;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Consumer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

public class SecurityFilterTest {
    private String testNotAuthorized(MultivaluedMap<String, String> headers) {
        UriInfo uriInfo = Mockito.mock(UriInfo.class);
        Mockito.when(uriInfo.getRequestUri()).thenReturn(URI.create("https://tutorialapi.com/api/lists"));

        ContainerRequestContext containerRequestContext = Mockito.mock(ContainerRequestContext.class);
        Mockito.when(containerRequestContext.getUriInfo()).thenReturn(uriInfo);
        Mockito.when(containerRequestContext.getHeaders()).thenReturn(headers);

        Properties configProperties = new Properties();
        configProperties.setProperty(ConfigKey.RAPIDAPI_PROXY_SECRET.getKey(), "secret");
        Config config = ConfigFactory.parseProperties(configProperties);

        ServiceFactory serviceFactory = Mockito.mock(ServiceFactory.class);

        NotAuthorizedException notAuthorized = Assertions.assertThrows(NotAuthorizedException.class,
                () -> new SecurityFilter(config, serviceFactory).filter(containerRequestContext));

        return notAuthorized.getMessage();
    }

    @Test
    public void testFilterNoHeaders() {
        String errorMessage = testNotAuthorized(new MultivaluedHashMap<>());
        Assertions.assertEquals("Missing security header: X-RapidAPI-Proxy-Secret", errorMessage);
    }

    @Test
    public void testFilterWithProxySecretHeader() {
        MultivaluedMap<String, String> headers = new MultivaluedHashMap<>();
        headers.putSingle(SecurityHeader.RAPID_API_PROXY_SECRET.getHeader(), "proxy-secret");
        String errorMessage = testNotAuthorized(headers);
        Assertions.assertEquals("Missing security header: X-RapidAPI-User", errorMessage);
    }

    @Test
    public void testFilterWithProxySecretAndUserHeaders() {
        MultivaluedMap<String, String> headers = new MultivaluedHashMap<>();
        headers.putSingle(SecurityHeader.RAPID_API_PROXY_SECRET.getHeader(), "proxy-secret");
        headers.putSingle(SecurityHeader.RAPID_API_USER.getHeader(), "user");
        String errorMessage = testNotAuthorized(headers);
        Assertions.assertEquals("Missing or invalid security header: X-RapidAPI-Subscription", errorMessage);
    }

    @Test
    public void testFilterWithInvalidSubscription() {
        MultivaluedMap<String, String> headers = new MultivaluedHashMap<>();
        headers.putSingle(SecurityHeader.RAPID_API_PROXY_SECRET.getHeader(), "proxy-secret");
        headers.putSingle(SecurityHeader.RAPID_API_USER.getHeader(), "user");
        headers.putSingle(SecurityHeader.RAPID_API_SUBSCRIPTION.getHeader(), "invalid");
        String errorMessage = testNotAuthorized(headers);
        Assertions.assertEquals("Missing or invalid security header: X-RapidAPI-Subscription", errorMessage);
    }

    @Test
    public void testFilterInvalidProxySecret() {
        RapidApiPrincipal principal = new RapidApiPrincipal("invalid", "user", Subscription.BASIC);

        UriInfo uriInfo = Mockito.mock(UriInfo.class);
        Mockito.when(uriInfo.getRequestUri()).thenReturn(URI.create("https://tutorialapi.com/api/lists"));

        MultivaluedMap<String, String> headers = new MultivaluedHashMap<>();
        headers.putSingle(SecurityHeader.RAPID_API_PROXY_SECRET.getHeader(), principal.getProxySecret());
        headers.putSingle(SecurityHeader.RAPID_API_USER.getHeader(), principal.getUser());
        headers.putSingle(SecurityHeader.RAPID_API_SUBSCRIPTION.getHeader(), principal.getSubscription().name());

        ContainerRequestContext containerRequestContext = Mockito.mock(ContainerRequestContext.class);
        Mockito.when(containerRequestContext.getUriInfo()).thenReturn(uriInfo);
        Mockito.when(containerRequestContext.getHeaders()).thenReturn(headers);

        Properties configProperties = new Properties();
        configProperties.setProperty(ConfigKey.RAPIDAPI_PROXY_SECRET.getKey(), "proxy-secret");
        Config config = ConfigFactory.parseProperties(configProperties);

        ServiceFactory serviceFactory = Mockito.mock(ServiceFactory.class);

        NotAuthorizedException notAuthorized = Assertions.assertThrows(NotAuthorizedException.class,
                () -> new SecurityFilter(config, serviceFactory).filter(containerRequestContext));

        Assertions.assertEquals("Invalid proxy secret", notAuthorized.getMessage());

        Mockito.verify(containerRequestContext, Mockito.times(0)).setSecurityContext(any());
    }

    @Test
    public void testFilterSuccessful() {
        RapidApiPrincipal principal = new RapidApiPrincipal("proxy-secret", "user", Subscription.BASIC);

        UriInfo uriInfo = Mockito.mock(UriInfo.class);
        Mockito.when(uriInfo.getRequestUri()).thenReturn(URI.create("https://tutorialapi.com/api/lists"));

        MultivaluedMap<String, String> headers = new MultivaluedHashMap<>();
        headers.putSingle(SecurityHeader.RAPID_API_PROXY_SECRET.getHeader(), principal.getProxySecret());
        headers.putSingle(SecurityHeader.RAPID_API_USER.getHeader(), principal.getUser());
        headers.putSingle(SecurityHeader.RAPID_API_SUBSCRIPTION.getHeader(), principal.getSubscription().name());

        ContainerRequestContext containerRequestContext = Mockito.mock(ContainerRequestContext.class);
        Mockito.when(containerRequestContext.getUriInfo()).thenReturn(uriInfo);
        Mockito.when(containerRequestContext.getHeaders()).thenReturn(headers);

        Properties configProperties = new Properties();
        configProperties.setProperty(ConfigKey.RAPIDAPI_PROXY_SECRET.getKey(), "proxy-secret");
        Config config = ConfigFactory.parseProperties(configProperties);

        ServiceFactory serviceFactory = Mockito.mock(ServiceFactory.class);

        new SecurityFilter(config, serviceFactory).filter(containerRequestContext);

        RapidApiSecurityContext securityContext = new RapidApiSecurityContext(principal);
        Mockito.verify(containerRequestContext, Mockito.times(1)).setSecurityContext(ArgumentMatchers.eq(securityContext));
    }

    private void testApiKey(ApiKeyService apiKeyService, Consumer<ContainerRequestContext> consumer) {
        RapidApiPrincipal principal = new RapidApiPrincipal("key", "user", Subscription.BASIC);

        UriInfo uriInfo = Mockito.mock(UriInfo.class);
        Mockito.when(uriInfo.getRequestUri()).thenReturn(URI.create("https://tutorialapi.com/api/lists"));

        MultivaluedMap<String, String> headers = new MultivaluedHashMap<>();
        headers.putSingle(SecurityHeader.TUTORIAL_API_KEY.getHeader(), "key");

        ContainerRequestContext containerRequestContext = Mockito.mock(ContainerRequestContext.class);
        Mockito.when(containerRequestContext.getUriInfo()).thenReturn(uriInfo);
        Mockito.when(containerRequestContext.getHeaders()).thenReturn(headers);

        ServiceFactory serviceFactory = Mockito.mock(ServiceFactory.class);
        Mockito.when(serviceFactory.getApiKeyService()).thenReturn(apiKeyService);

        new SecurityFilter(ConfigFactory.empty(), serviceFactory).filter(containerRequestContext);

        consumer.accept(containerRequestContext);
    }

    @Test
    public void testApiKeyNotFound() {
        ApiKeyService apiKeyService = Mockito.mock(ApiKeyService.class);
        Mockito.when(apiKeyService.get(eq("key"))).thenReturn(Optional.empty());

        NotAuthorizedException notAuthorized = Assertions.assertThrows(NotAuthorizedException.class,
                () -> testApiKey(apiKeyService, requestContext -> {
                    Mockito.verify(requestContext, Mockito.times(0)).setSecurityContext(any());
                }));

        Assertions.assertEquals("Invalid API Key", notAuthorized.getMessage());
    }

    @Test
    public void testApiKeySuccessful() {
        ApiKeyService apiKeyService = Mockito.mock(ApiKeyService.class);
        ApiKey apikey = new ApiKey().setApikey("key").setUser("user").setSubscription(Subscription.BASIC);
        Mockito.when(apiKeyService.get(eq("key"))).thenReturn(Optional.of(apikey));

        testApiKey(apiKeyService, requestContext -> {
            RapidApiPrincipal principal = new RapidApiPrincipal("key", "user", Subscription.BASIC);
            RapidApiSecurityContext securityContext = new RapidApiSecurityContext(principal);
            Mockito.verify(requestContext, Mockito.times(1)).setSecurityContext(ArgumentMatchers.eq(securityContext));
        });
    }
}
