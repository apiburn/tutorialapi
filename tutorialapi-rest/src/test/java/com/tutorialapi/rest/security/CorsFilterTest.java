package com.tutorialapi.rest.security;

import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class CorsFilterTest {
    @Test
    public void testFilter() {
        MultivaluedMap<String, Object> headerMap = new MultivaluedHashMap<>();
        ContainerResponseContext containerResponseContext = Mockito.mock(ContainerResponseContext.class);
        Mockito.when(containerResponseContext.getHeaders()).thenReturn(headerMap);

        new CorsFilter().filter(null, containerResponseContext);

        Assertions.assertEquals(2, headerMap.size());
        Assertions.assertTrue(headerMap.containsKey("Access-Control-Allow-Origin"));
        Assertions.assertTrue(headerMap.containsKey("Access-Control-Allow-Methods"));
        Assertions.assertEquals("[*]", headerMap.get("Access-Control-Allow-Origin").toString());
        Assertions.assertEquals("[DELETE, HEAD, GET, OPTIONS, PATCH, POST, PUT]",
                headerMap.get("Access-Control-Allow-Methods").toString());
    }
}
