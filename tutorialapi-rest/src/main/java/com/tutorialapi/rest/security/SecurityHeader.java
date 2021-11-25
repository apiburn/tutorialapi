package com.tutorialapi.rest.security;

public enum SecurityHeader {
    RAPID_API_PROXY_SECRET("X-RapidAPI-Proxy-Secret"),
    RAPID_API_USER("X-RapidAPI-User"),
    RAPID_API_SUBSCRIPTION("X-RapidAPI-Subscription");

    private final String header;

    SecurityHeader(String header) {
        this.header = header;
    }

    public String getHeader() {
        return header;
    }
}
