package com.tutorialapi.model;

import java.util.Optional;

public enum Subscription {
    BASIC,
    PRO,
    ULTRA,
    MEGA,
    CUSTOM;

    public static Optional<Subscription> from(String value) {
        if (value != null) {
            for (Subscription subscription : values()) {
                if (subscription.name().equalsIgnoreCase(value)) {
                    return Optional.of(subscription);
                }
            }
        }
        return Optional.empty();
    }
}
