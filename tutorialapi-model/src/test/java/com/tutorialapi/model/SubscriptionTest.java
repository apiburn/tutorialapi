package com.tutorialapi.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Optional;

public class SubscriptionTest {
    @Test
    public void testFrom() {
        Assertions.assertEquals(Optional.of(Subscription.BASIC), Subscription.from("BASIC"));
        Assertions.assertEquals(Optional.of(Subscription.BASIC), Subscription.from("basic"));
        Assertions.assertEquals(Optional.empty(), Subscription.from("asdf"));
        Assertions.assertEquals(Optional.empty(), Subscription.from(null));
    }
}
