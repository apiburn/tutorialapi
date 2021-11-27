package com.tutorialapi.rest.resource.v1.items;

import com.tutorialapi.db.ServiceFactory;
import com.tutorialapi.db.service.TodoItemService;
import com.tutorialapi.model.TodoItem;
import com.tutorialapi.model.user.RapidApiPrincipal;
import com.tutorialapi.model.user.Subscription;
import com.tutorialapi.rest.ApiApplication;
import com.tutorialapi.rest.resource.v1.BaseResourceIT;
import com.tutorialapi.rest.security.SecurityHeader;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

public class DeleteTodoItemResourceIT extends BaseResourceIT {
    private TodoItemService todoItemService;

    @Override
    protected Application configure() {
        ServiceFactory serviceFactory = Mockito.mock(ServiceFactory.class);
        todoItemService = Mockito.mock(TodoItemService.class);
        Mockito.when(serviceFactory.getTodoItemService()).thenReturn(todoItemService);

        return new ApiApplication(serviceFactory);
    }

    @Test
    public void testNoSecurityHeaders() {
        Response response = target("/v1/lists/list-id/items/item-id").request().delete();
        verifyErrorResponse(response, Response.Status.UNAUTHORIZED.getStatusCode(),
                "Missing security header: X-RapidAPI-Proxy-Secret");
        Mockito.verify(todoItemService, Mockito.times(0)).delete(any(), any(), any());
    }

    @Test
    public void testOnlyProxySecretHeader() {
        Response response = target("/v1/lists/list-id/items/item-id").request()
                .header(SecurityHeader.RAPID_API_PROXY_SECRET.getHeader(), "proxy-secret")
                .delete();
        verifyErrorResponse(response, Response.Status.UNAUTHORIZED.getStatusCode(),
                "Missing security header: X-RapidAPI-User");
        Mockito.verify(todoItemService, Mockito.times(0)).delete(any(), any(), any());
    }

    @Test
    public void testProxySecretAndUserHeader() {
        Response response = target("/v1/lists/list-id/items/item-id").request()
                .header(SecurityHeader.RAPID_API_PROXY_SECRET.getHeader(), "proxy-secret")
                .header(SecurityHeader.RAPID_API_USER.getHeader(), "user")
                .delete();
        verifyErrorResponse(response, Response.Status.UNAUTHORIZED.getStatusCode(),
                "Missing or invalid security header: X-RapidAPI-Subscription");
        Mockito.verify(todoItemService, Mockito.times(0)).delete(any(), any(), any());
    }

    @Test
    public void testInvalidSubscription() {
        Response response = target("/v1/lists/list-id/items/item-id").request()
                .header(SecurityHeader.RAPID_API_PROXY_SECRET.getHeader(), "proxy-secret")
                .header(SecurityHeader.RAPID_API_USER.getHeader(), "user")
                .header(SecurityHeader.RAPID_API_SUBSCRIPTION.getHeader(), "invalid")
                .delete();
        verifyErrorResponse(response, Response.Status.UNAUTHORIZED.getStatusCode(),
                "Missing or invalid security header: X-RapidAPI-Subscription");
        Mockito.verify(todoItemService, Mockito.times(0)).delete(any(), any(), any());
    }

    @Test
    public void testMissingTodoItem() {
        RapidApiPrincipal principal = new RapidApiPrincipal("proxy-secret", "user", Subscription.BASIC);
        Mockito.when(todoItemService.delete(eq(principal), eq("list-id"), eq("item-id"))).thenReturn(Optional.empty());

        Response response = target("/v1/lists/list-id/items/item-id").request()
                .header(SecurityHeader.RAPID_API_PROXY_SECRET.getHeader(), "proxy-secret")
                .header(SecurityHeader.RAPID_API_USER.getHeader(), "user")
                .header(SecurityHeader.RAPID_API_SUBSCRIPTION.getHeader(), Subscription.BASIC.name())
                .delete();
        verifyErrorResponse(response, Response.Status.NOT_FOUND.getStatusCode(),
                "Item with id item-id not found in list with id list-id");
        Mockito.verify(todoItemService, Mockito.times(1)).delete(eq(principal), eq("list-id"), eq("item-id"));
    }

    @Test
    public void testTodoItemFound() {
        TodoItem item = new TodoItem().setId("item-id").setTask("Task 1").setDone(false);
        RapidApiPrincipal principal = new RapidApiPrincipal("proxy-secret", "user", Subscription.BASIC);
        Mockito.when(todoItemService.delete(eq(principal), eq("list-id"), eq("item-id"))).thenReturn(Optional.of(item));

        Response response = target("/v1/lists/list-id/items/item-id").request()
                .header(SecurityHeader.RAPID_API_PROXY_SECRET.getHeader(), "proxy-secret")
                .header(SecurityHeader.RAPID_API_USER.getHeader(), "user")
                .header(SecurityHeader.RAPID_API_SUBSCRIPTION.getHeader(), Subscription.BASIC.name())
                .delete();

        Assertions.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        Assertions.assertEquals(MediaType.APPLICATION_JSON_TYPE, response.getMediaType());
        Assertions.assertEquals(item, response.readEntity(TodoItem.class));
        verifyCorsHeaders(response);
        Mockito.verify(todoItemService, Mockito.times(1)).delete(eq(principal), eq("list-id"), eq("item-id"));
    }

    @Test
    public void testServiceException() {
        RapidApiPrincipal principal = new RapidApiPrincipal("proxy-secret", "user", Subscription.BASIC);
        Mockito.when(todoItemService.delete(eq(principal), eq("list-id"), eq("item-id")))
                .thenThrow(new RuntimeException("Failed"));

        Response response = target("/v1/lists/list-id/items/item-id").request()
                .header(SecurityHeader.RAPID_API_PROXY_SECRET.getHeader(), "proxy-secret")
                .header(SecurityHeader.RAPID_API_USER.getHeader(), "user")
                .header(SecurityHeader.RAPID_API_SUBSCRIPTION.getHeader(), Subscription.BASIC.name())
                .delete();
        verifyErrorResponse(response, Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), "Failed");
        Mockito.verify(todoItemService, Mockito.times(1)).delete(eq(principal), eq("list-id"), eq("item-id"));
    }
}
