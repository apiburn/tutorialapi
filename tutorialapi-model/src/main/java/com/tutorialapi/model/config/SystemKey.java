package com.tutorialapi.model.config;

public enum SystemKey implements Key {
    PORT("8443"),
    MODE("dev");

    private final String defaultValue;

    SystemKey(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getDefaultValue() {
        return defaultValue;
    }
}
