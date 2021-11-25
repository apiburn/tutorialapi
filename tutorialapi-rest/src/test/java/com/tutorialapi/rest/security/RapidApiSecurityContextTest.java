package com.tutorialapi.rest.security;

import com.tutorialapi.model.RapidApiPrincipal;
import com.tutorialapi.model.Subscription;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class RapidApiSecurityContextTest {
    @Test
    public void testConstructor() {
        RapidApiPrincipal principal = new RapidApiPrincipal("proxy-secret", "user", Subscription.BASIC);
        RapidApiSecurityContext securityContext = new RapidApiSecurityContext(principal);

        Assertions.assertEquals(principal, securityContext.getUserPrincipal());
        Assertions.assertTrue(securityContext.isUserInRole("BASIC"));
        Assertions.assertTrue(securityContext.isSecure());
        Assertions.assertEquals("RapidAPI", securityContext.getAuthenticationScheme());
    }
}
