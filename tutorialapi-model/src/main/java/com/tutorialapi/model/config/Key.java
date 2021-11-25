package com.tutorialapi.model.config;

import static java.util.Locale.ENGLISH;

public interface Key {
    String name();

    default String getKey() {
        return name().toLowerCase(ENGLISH).replaceAll("_", ".");
    }
}
