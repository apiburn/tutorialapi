package com.tutorialapi.model;

import java.security.Principal;
import java.util.Objects;

public class RapidApiPrincipal implements Principal {
    private final String proxySecret;
    private final String user;
    private final Subscription subscription;

    public RapidApiPrincipal(String proxySecret, String user, Subscription subscription) {
        this.proxySecret = proxySecret;
        this.user = user;
        this.subscription = subscription;
    }

    @Override
    public String getName() {
        return user;
    }

    public String getProxySecret() {
        return proxySecret;
    }

    public String getUser() {
        return user;
    }

    public Subscription getSubscription() {
        return subscription;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RapidApiPrincipal that = (RapidApiPrincipal) o;
        return Objects.equals(proxySecret, that.proxySecret) &&
                Objects.equals(user, that.user) &&
                subscription == that.subscription;
    }

    @Override
    public int hashCode() {
        return Objects.hash(proxySecret, user, subscription);
    }

    @Override
    public String toString() {
        return "RapidApiPrincipal{" +
                "proxySecret='" + proxySecret + '\'' +
                ", user='" + user + '\'' +
                ", subscription=" + subscription +
                '}';
    }
}
