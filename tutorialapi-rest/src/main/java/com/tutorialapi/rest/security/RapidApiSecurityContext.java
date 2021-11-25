package com.tutorialapi.rest.security;

import com.tutorialapi.model.user.RapidApiPrincipal;
import com.tutorialapi.model.user.Subscription;
import jakarta.ws.rs.core.SecurityContext;

import java.security.Principal;
import java.util.Objects;

public class RapidApiSecurityContext implements SecurityContext {
    private static final String AUTHENTICATION_SCHEME = "RapidAPI";

    private final RapidApiPrincipal principal;

    public RapidApiSecurityContext(RapidApiPrincipal principal) {
        this.principal = principal;
    }

    @Override
    public Principal getUserPrincipal() {
        return principal;
    }

    @Override
    public boolean isUserInRole(String role) {
        return principal.getSubscription() == Subscription.from(role).orElse(null);
    }

    @Override
    public boolean isSecure() {
        return true;
    }

    @Override
    public String getAuthenticationScheme() {
        return AUTHENTICATION_SCHEME;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RapidApiSecurityContext that = (RapidApiSecurityContext) o;
        return Objects.equals(principal, that.principal);
    }

    @Override
    public int hashCode() {
        return Objects.hash(principal);
    }

    @Override
    public String toString() {
        return "RapidApiSecurityContext{" +
                "principal=" + principal +
                '}';
    }
}
