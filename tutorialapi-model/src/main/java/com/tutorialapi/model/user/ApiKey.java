package com.tutorialapi.model.user;

import java.util.Objects;

public class ApiKey {
    private String apikey;
    private String user;
    private Subscription subscription;

    public String getApikey() {
        return apikey;
    }

    public ApiKey setApikey(String apikey) {
        this.apikey = apikey;
        return this;
    }

    public String getUser() {
        return user;
    }

    public ApiKey setUser(String user) {
        this.user = user;
        return this;
    }

    public Subscription getSubscription() {
        return subscription;
    }

    public ApiKey setSubscription(Subscription subscription) {
        this.subscription = subscription;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ApiKey apiKey = (ApiKey) o;
        return Objects.equals(apikey, apiKey.apikey) &&
                Objects.equals(user, apiKey.user) &&
                subscription == apiKey.subscription;
    }

    @Override
    public int hashCode() {
        return Objects.hash(apikey, user, subscription);
    }

    @Override
    public String toString() {
        return "ApiKey{" +
                "apikey='" + apikey + '\'' +
                ", user='" + user + '\'' +
                ", subscription=" + subscription +
                '}';
    }
}
