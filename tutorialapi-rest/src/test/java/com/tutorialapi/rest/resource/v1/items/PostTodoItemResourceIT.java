package com.tutorialapi.rest.resource.v1.items;

import com.tutorialapi.db.ServiceFactory;
import com.tutorialapi.db.exception.ConflictException;
import com.tutorialapi.db.service.TodoItemService;
import com.tutorialapi.model.TodoItem;
import com.tutorialapi.model.user.RapidApiPrincipal;
import com.tutorialapi.model.user.Subscription;
import com.tutorialapi.rest.ApiApplication;
import com.tutorialapi.rest.resource.v1.BaseResourceIT;
import com.tutorialapi.rest.security.SecurityHeader;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

public class PostTodoItemResourceIT extends BaseResourceIT {
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
        TodoItem todoItem = new TodoItem().setId("item-id").setTask("Item Task").setDone(false);
        Entity<TodoItem> entity = Entity.entity(todoItem, MediaType.APPLICATION_JSON_TYPE);
        Response response = target("/v1/lists/list-id/items").request().post(entity);
        verifyErrorResponse(response, Response.Status.UNAUTHORIZED.getStatusCode(),
                "Missing security header: X-RapidAPI-Proxy-Secret");
        Mockito.verify(todoItemService, Mockito.times(0)).create(any(), any(), any());
    }

    @Test
    public void testOnlyProxySecretHeader() {
        TodoItem todoItem = new TodoItem().setId("item-id").setTask("Item Task").setDone(false);
        Entity<TodoItem> entity = Entity.entity(todoItem, MediaType.APPLICATION_JSON_TYPE);
        Response response = target("/v1/lists/list-id/items").request()
                .header(SecurityHeader.RAPID_API_PROXY_SECRET.getHeader(), "proxy-secret")
                .post(entity);
        verifyErrorResponse(response, Response.Status.UNAUTHORIZED.getStatusCode(),
                "Missing security header: X-RapidAPI-User");
        Mockito.verify(todoItemService, Mockito.times(0)).create(any(), any(), any());
    }

    @Test
    public void testProxySecretAndUserHeader() {
        TodoItem todoItem = new TodoItem().setId("item-id").setTask("Item Task").setDone(false);
        Entity<TodoItem> entity = Entity.entity(todoItem, MediaType.APPLICATION_JSON_TYPE);
        Response response = target("/v1/lists/list-id/items").request()
                .header(SecurityHeader.RAPID_API_PROXY_SECRET.getHeader(), "proxy-secret")
                .header(SecurityHeader.RAPID_API_USER.getHeader(), "user")
                .post(entity);
        verifyErrorResponse(response, Response.Status.UNAUTHORIZED.getStatusCode(),
                "Missing or invalid security header: X-RapidAPI-Subscription");
        Mockito.verify(todoItemService, Mockito.times(0)).create(any(), any(), any());
    }

    @Test
    public void testInvalidSubscription() {
        TodoItem todoItem = new TodoItem().setId("item-id").setTask("Item Task").setDone(false);
        Entity<TodoItem> entity = Entity.entity(todoItem, MediaType.APPLICATION_JSON_TYPE);
        Response response = target("/v1/lists/list-id/items").request()
                .header(SecurityHeader.RAPID_API_PROXY_SECRET.getHeader(), "proxy-secret")
                .header(SecurityHeader.RAPID_API_USER.getHeader(), "user")
                .header(SecurityHeader.RAPID_API_SUBSCRIPTION.getHeader(), "invalid")
                .post(entity);
        verifyErrorResponse(response, Response.Status.UNAUTHORIZED.getStatusCode(),
                "Missing or invalid security header: X-RapidAPI-Subscription");
        Mockito.verify(todoItemService, Mockito.times(0)).create(any(), any(), any());
    }

    @Test
    public void testTodoItemCreateFalse() {
        TodoItem todoItem = new TodoItem().setId("item-id").setTask("Item Task").setDone(false);
        RapidApiPrincipal principal = new RapidApiPrincipal("proxy-secret", "user", Subscription.BASIC);
        Mockito.when(todoItemService.create(eq(principal), eq("list-id"), eq(todoItem))).thenReturn(false);

        Entity<TodoItem> entity = Entity.entity(todoItem, MediaType.APPLICATION_JSON_TYPE);
        Response response = target("/v1/lists/list-id/items").request()
                .header(SecurityHeader.RAPID_API_PROXY_SECRET.getHeader(), "proxy-secret")
                .header(SecurityHeader.RAPID_API_USER.getHeader(), "user")
                .header(SecurityHeader.RAPID_API_SUBSCRIPTION.getHeader(), Subscription.BASIC.name())
                .post(entity);
        verifyErrorResponse(response, Response.Status.BAD_REQUEST.getStatusCode(),
                "Invalid input, failed to insert todo item");
        Mockito.verify(todoItemService, Mockito.times(1)).create(eq(principal), eq("list-id"), eq(todoItem));
    }

    @Test
    public void testTodoItemCreateTrue() {
        TodoItem todoItem = new TodoItem().setId("item-id").setTask("Item Task").setDone(false);
        RapidApiPrincipal principal = new RapidApiPrincipal("proxy-secret", "user", Subscription.BASIC);
        Mockito.when(todoItemService.create(eq(principal), eq("list-id"), eq(todoItem))).thenReturn(true);

        Entity<TodoItem> entity = Entity.entity(todoItem, MediaType.APPLICATION_JSON_TYPE);
        Response response = target("/v1/lists/list-id/items").request()
                .header(SecurityHeader.RAPID_API_PROXY_SECRET.getHeader(), "proxy-secret")
                .header(SecurityHeader.RAPID_API_USER.getHeader(), "user")
                .header(SecurityHeader.RAPID_API_SUBSCRIPTION.getHeader(), Subscription.BASIC.name())
                .post(entity);

        Assertions.assertEquals(200, response.getStatus());
        Assertions.assertEquals(MediaType.APPLICATION_JSON_TYPE, response.getMediaType());
        Assertions.assertEquals(todoItem, response.readEntity(TodoItem.class));
        verifyCorsHeaders(response);
        Mockito.verify(todoItemService, Mockito.times(1)).create(eq(principal), eq("list-id"), eq(todoItem));
    }

    @Test
    public void testTodoItemNoId() {
        RapidApiPrincipal principal = new RapidApiPrincipal("proxy-secret", "user", Subscription.BASIC);
        Mockito.when(todoItemService.create(eq(principal), eq("list-id"), any())).thenReturn(true);

        TodoItem todoItem = new TodoItem().setTask("Item Task").setDone(false);
        Entity<TodoItem> entity = Entity.entity(todoItem, MediaType.APPLICATION_JSON_TYPE);
        Response response = target("/v1/lists/list-id/items").request()
                .header(SecurityHeader.RAPID_API_PROXY_SECRET.getHeader(), "proxy-secret")
                .header(SecurityHeader.RAPID_API_USER.getHeader(), "user")
                .header(SecurityHeader.RAPID_API_SUBSCRIPTION.getHeader(), Subscription.BASIC.name())
                .post(entity);

        Assertions.assertEquals(200, response.getStatus());
        Assertions.assertEquals(MediaType.APPLICATION_JSON_TYPE, response.getMediaType());
        TodoItem result = response.readEntity(TodoItem.class);
        Assertions.assertNotNull(result.getId());
        Assertions.assertEquals(todoItem.getTask(), result.getTask());
        Assertions.assertEquals(todoItem.isDone(), result.isDone());
        verifyCorsHeaders(response);
        Mockito.verify(todoItemService, Mockito.times(1)).create(eq(principal), eq("list-id"), eq(result));
    }

    @Test
    public void testConflictException() {
        TodoItem todoItem = new TodoItem().setId("item-id").setTask("Item Task").setDone(false);
        RapidApiPrincipal principal = new RapidApiPrincipal("proxy-secret", "user", Subscription.BASIC);
        Mockito.when(todoItemService.create(eq(principal), eq("list-id"), eq(todoItem)))
                .thenThrow(new ConflictException("Already exists"));

        Entity<TodoItem> entity = Entity.entity(todoItem, MediaType.APPLICATION_JSON_TYPE);
        Response response = target("/v1/lists/list-id/items").request()
                .header(SecurityHeader.RAPID_API_PROXY_SECRET.getHeader(), "proxy-secret")
                .header(SecurityHeader.RAPID_API_USER.getHeader(), "user")
                .header(SecurityHeader.RAPID_API_SUBSCRIPTION.getHeader(), Subscription.BASIC.name())
                .post(entity);
        verifyErrorResponse(response, Response.Status.CONFLICT.getStatusCode(), "Already exists");
        Mockito.verify(todoItemService, Mockito.times(1)).create(eq(principal), eq("list-id"), eq(todoItem));
    }

    @Test
    public void testServiceException() {
        TodoItem todoItem = new TodoItem().setId("item-id").setTask("Item Task").setDone(false);
        RapidApiPrincipal principal = new RapidApiPrincipal("proxy-secret", "user", Subscription.BASIC);
        Mockito.when(todoItemService.create(eq(principal), eq("list-id"), eq(todoItem)))
                .thenThrow(new RuntimeException("Failed"));

        Entity<TodoItem> entity = Entity.entity(todoItem, MediaType.APPLICATION_JSON_TYPE);
        Response response = target("/v1/lists/list-id/items").request()
                .header(SecurityHeader.RAPID_API_PROXY_SECRET.getHeader(), "proxy-secret")
                .header(SecurityHeader.RAPID_API_USER.getHeader(), "user")
                .header(SecurityHeader.RAPID_API_SUBSCRIPTION.getHeader(), Subscription.BASIC.name())
                .post(entity);
        verifyErrorResponse(response, Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), "Failed");
        Mockito.verify(todoItemService, Mockito.times(1)).create(eq(principal), eq("list-id"), eq(todoItem));
    }
}
