package com.tutorialapi.rest.security;

import com.tutorialapi.model.RapidApiPrincipal;
import com.tutorialapi.model.Subscription;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.UriInfo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static java.util.Optional.ofNullable;

public class AccessLogFilterTest {
    private String test(RapidApiPrincipal principal, String method, String path) {
        List<String> logList = new ArrayList<>();
        AccessLogFilter accessLogFilter = new AccessLogFilter(logList::add);

        RapidApiSecurityContext securityContext = ofNullable(principal).map(RapidApiSecurityContext::new).orElse(null);
        UriInfo uriInfo = Mockito.mock(UriInfo.class);
        Mockito.when(uriInfo.getAbsolutePath()).thenReturn(URI.create("https://localhost" + path));
        ContainerRequestContext containerRequestContext = Mockito.mock(ContainerRequestContext.class);
        Mockito.when(containerRequestContext.getSecurityContext()).thenReturn(securityContext);
        Mockito.when(containerRequestContext.getMethod()).thenReturn(method);
        Mockito.when(containerRequestContext.getUriInfo()).thenReturn(uriInfo);
        accessLogFilter.filter(containerRequestContext);

        Assertions.assertEquals(1, logList.size());
        return logList.get(0);
    }

    @Test
    public void testFilterNoUser() {
        String log = test(null, "GET", "/test");
        Assertions.assertEquals("? => GET /test", log);
    }

    @Test
    public void testFilterWithUser() {
        RapidApiPrincipal principal = new RapidApiPrincipal("proxy-secret", "user", Subscription.BASIC);

        String log = test(principal, "GET", "/test");
        Assertions.assertEquals("user => GET /test", log);
    }

    @Test
    public void testFilterPost() {
        String log = test(null, "POST", "/test/id");
        Assertions.assertEquals("? => POST /test/id", log);
    }
}
