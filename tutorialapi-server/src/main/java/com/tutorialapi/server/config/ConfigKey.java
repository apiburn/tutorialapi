package com.tutorialapi.server.config;

import static java.util.Locale.ENGLISH;

public enum ConfigKey {
    SERVER_KEYSTORE_FILE,
    SERVER_KEYSTORE_TYPE,
    SERVER_KEYSTORE_PASSWORD,
    SERVER_WEB_CONTENT;

    public String getKey() {
        return name().toLowerCase(ENGLISH).replaceAll("_", ".");
    }
}
