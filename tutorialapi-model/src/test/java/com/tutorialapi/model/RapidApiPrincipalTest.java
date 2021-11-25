package com.tutorialapi.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class RapidApiPrincipalTest {
    @Test
    public void testConstructor() {
        RapidApiPrincipal principal = new RapidApiPrincipal("proxy-secret", "user", Subscription.BASIC);

        Assertions.assertEquals("proxy-secret", principal.getProxySecret());
        Assertions.assertEquals("user", principal.getUser());
        Assertions.assertEquals("user", principal.getName());
        Assertions.assertEquals(Subscription.BASIC, principal.getSubscription());
    }

    @Test
    public void testEquals() {
        RapidApiPrincipal principal1 = new RapidApiPrincipal("proxy-secret", "user1", Subscription.BASIC);
        RapidApiPrincipal principal2 = new RapidApiPrincipal("proxy-secret", "user1", Subscription.BASIC);
        RapidApiPrincipal principal3 = new RapidApiPrincipal("proxy-secret", "user2", Subscription.PRO);

        Assertions.assertEquals(principal1, principal1);
        Assertions.assertEquals(principal1, principal2);
        Assertions.assertNotEquals(principal1, principal3);
    }

    @Test
    public void testHashCode() {
        RapidApiPrincipal principal1 = new RapidApiPrincipal("proxy-secret", "user1", Subscription.BASIC);
        RapidApiPrincipal principal2 = new RapidApiPrincipal("proxy-secret", "user1", Subscription.BASIC);
        RapidApiPrincipal principal3 = new RapidApiPrincipal("proxy-secret", "user2", Subscription.PRO);

        Assertions.assertEquals(principal1.hashCode(), principal1.hashCode());
        Assertions.assertEquals(principal1.hashCode(), principal2.hashCode());
        Assertions.assertNotEquals(principal1.hashCode(), principal3.hashCode());
    }

    @Test
    public void testToString() {
        RapidApiPrincipal principal = new RapidApiPrincipal("proxy-secret", "user", Subscription.BASIC);

        String expected = "RapidApiPrincipal{proxySecret='proxy-secret', user='user', subscription=BASIC}";
        Assertions.assertEquals(expected, principal.toString());
    }
}
