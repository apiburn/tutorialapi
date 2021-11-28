package com.tutorialapi.rest.security;

import com.tutorialapi.model.config.ConfigKey;
import com.tutorialapi.model.user.ApiKey;
import com.tutorialapi.model.user.RapidApiPrincipal;
import com.tutorialapi.model.user.Subscription;
import com.tutorialapi.rest.Environment;
import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static jakarta.ws.rs.core.Response.Status.UNAUTHORIZED;

@Provider
@Priority(1)
public class SecurityFilter implements ContainerRequestFilter {
    private final Supplier<Environment> environmentSupplier;

    @Inject
    public SecurityFilter(Supplier<Environment> environmentSupplier) {
        this.environmentSupplier = environmentSupplier;
    }

    private Optional<String> getHeader(ContainerRequestContext context, String headerName) {
        return Stream.of(context.getHeaders())
                .filter(Objects::nonNull)
                .map(Map::entrySet)
                .flatMap(Collection::stream)
                .filter(entry -> entry.getKey().equalsIgnoreCase(headerName))
                .map(Entry::getValue)
                .flatMap(Collection::stream)
                .findFirst();
    }

    @Override
    public void filter(ContainerRequestContext containerRequestContext) {
        if (containerRequestContext.getUriInfo().getRequestUri().getPath().startsWith("/api/openapi")) {
            return;
        }

        RapidApiPrincipal principal = null;

        Optional<String> key = getHeader(containerRequestContext, SecurityHeader.TUTORIAL_API_KEY.getHeader());
        if (key.isPresent()) {
            ApiKey apikey = environmentSupplier.get().getServiceFactory().getApiKeyService().get(key.get())
                    .orElseThrow(() -> new NotAuthorizedException("Invalid API Key", Response.status(UNAUTHORIZED)));
            principal = new RapidApiPrincipal(apikey.getApikey(), apikey.getUser(), apikey.getSubscription());
        } else {
            Optional<String> proxySecret = getHeader(containerRequestContext, SecurityHeader.RAPID_API_PROXY_SECRET.getHeader());
            Optional<String> user = getHeader(containerRequestContext, SecurityHeader.RAPID_API_USER.getHeader());
            Optional<Subscription> subscription = getHeader(containerRequestContext,
                    SecurityHeader.RAPID_API_SUBSCRIPTION.getHeader()).flatMap(Subscription::from);

            if (proxySecret.isEmpty()) {
                throw new NotAuthorizedException("Missing security header: " +
                        SecurityHeader.RAPID_API_PROXY_SECRET.getHeader(), Response.status(UNAUTHORIZED));
            }
            if (user.isEmpty()) {
                throw new NotAuthorizedException("Missing security header: " +
                        SecurityHeader.RAPID_API_USER.getHeader(), Response.status(UNAUTHORIZED));
            }
            if (subscription.isEmpty()) {
                throw new NotAuthorizedException("Missing or invalid security header: " +
                        SecurityHeader.RAPID_API_SUBSCRIPTION.getHeader(), Response.status(UNAUTHORIZED));
            }

            String expectedProxySecret =
                    environmentSupplier.get().getConfig().getString(ConfigKey.RAPIDAPI_PROXY_SECRET.getKey());
            if (!proxySecret.get().equals(expectedProxySecret)) {
                throw new NotAuthorizedException("Invalid proxy secret", Response.status(UNAUTHORIZED));
            }
            principal = new RapidApiPrincipal(proxySecret.get(), user.get(), subscription.get());
        }

        containerRequestContext.setSecurityContext(new RapidApiSecurityContext(principal));
    }
}
