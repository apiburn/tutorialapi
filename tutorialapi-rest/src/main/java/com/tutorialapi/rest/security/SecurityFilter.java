package com.tutorialapi.rest.security;

import com.tutorialapi.model.user.RapidApiPrincipal;
import com.tutorialapi.model.user.Subscription;
import jakarta.annotation.Priority;
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
import java.util.stream.Stream;

@Provider
@Priority(1)
public class SecurityFilter implements ContainerRequestFilter {
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

        Optional<String> proxySecret = getHeader(containerRequestContext, SecurityHeader.RAPID_API_PROXY_SECRET.getHeader());
        Optional<String> user = getHeader(containerRequestContext, SecurityHeader.RAPID_API_USER.getHeader());
        Optional<Subscription> subscription = getHeader(containerRequestContext,
                SecurityHeader.RAPID_API_SUBSCRIPTION.getHeader()).flatMap(Subscription::from);

        if (proxySecret.isEmpty()) {
            throw new NotAuthorizedException("Missing security header: " +
                    SecurityHeader.RAPID_API_PROXY_SECRET.getHeader(), Response.status(Response.Status.UNAUTHORIZED));
        }
        if (user.isEmpty()) {
            throw new NotAuthorizedException("Missing security header: " +
                    SecurityHeader.RAPID_API_USER.getHeader(), Response.status(Response.Status.UNAUTHORIZED));
        }
        if (subscription.isEmpty()) {
            throw new NotAuthorizedException("Missing or invalid security header: " +
                    SecurityHeader.RAPID_API_SUBSCRIPTION.getHeader(), Response.status(Response.Status.UNAUTHORIZED));
        }

        RapidApiPrincipal principal = new RapidApiPrincipal(proxySecret.get(), user.get(), subscription.get());
        containerRequestContext.setSecurityContext(new RapidApiSecurityContext(principal));
    }
}
