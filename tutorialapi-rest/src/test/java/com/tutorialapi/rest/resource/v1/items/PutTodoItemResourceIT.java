package com.tutorialapi.rest.resource.v1.items;

import com.google.common.base.Strings;
import com.tutorialapi.db.ServiceFactory;
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

public class PutTodoItemResourceIT extends BaseResourceIT {
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
        Response response = target("/v1/lists/list-id/items/item-id").request().put(entity);
        verifyErrorResponse(response, Response.Status.UNAUTHORIZED.getStatusCode(),
                "Missing security header: X-RapidAPI-Proxy-Secret");
        Mockito.verify(todoItemService, Mockito.times(0)).update(any(), any(), any());
    }

    @Test
    public void testOnlyProxySecretHeader() {
        TodoItem todoItem = new TodoItem().setId("item-id").setTask("Item Task").setDone(false);
        Entity<TodoItem> entity = Entity.entity(todoItem, MediaType.APPLICATION_JSON_TYPE);
        Response response = target("/v1/lists/list-id/items/item-id").request()
                .header(SecurityHeader.RAPID_API_PROXY_SECRET.getHeader(), "proxy-secret")
                .put(entity);
        verifyErrorResponse(response, Response.Status.UNAUTHORIZED.getStatusCode(),
                "Missing security header: X-RapidAPI-User");
        Mockito.verify(todoItemService, Mockito.times(0)).update(any(), any(), any());
    }

    @Test
    public void testProxySecretAndUserHeader() {
        TodoItem todoItem = new TodoItem().setId("item-id").setTask("Item Task").setDone(false);
        Entity<TodoItem> entity = Entity.entity(todoItem, MediaType.APPLICATION_JSON_TYPE);
        Response response = target("/v1/lists/list-id/items/item-id").request()
                .header(SecurityHeader.RAPID_API_PROXY_SECRET.getHeader(), "proxy-secret")
                .header(SecurityHeader.RAPID_API_USER.getHeader(), "user")
                .put(entity);
        verifyErrorResponse(response, Response.Status.UNAUTHORIZED.getStatusCode(),
                "Missing or invalid security header: X-RapidAPI-Subscription");
        Mockito.verify(todoItemService, Mockito.times(0)).update(any(), any(), any());
    }

    @Test
    public void testInvalidSubscription() {
        TodoItem todoItem = new TodoItem().setId("item-id").setTask("Item Task").setDone(false);
        Entity<TodoItem> entity = Entity.entity(todoItem, MediaType.APPLICATION_JSON_TYPE);
        Response response = target("/v1/lists/list-id/items/item-id").request()
                .header(SecurityHeader.RAPID_API_PROXY_SECRET.getHeader(), "proxy-secret")
                .header(SecurityHeader.RAPID_API_USER.getHeader(), "user")
                .header(SecurityHeader.RAPID_API_SUBSCRIPTION.getHeader(), "invalid")
                .put(entity);
        verifyErrorResponse(response, Response.Status.UNAUTHORIZED.getStatusCode(),
                "Missing or invalid security header: X-RapidAPI-Subscription");
        Mockito.verify(todoItemService, Mockito.times(0)).update(any(), any(), any());
    }

    @Test
    public void testTodoItemUpdateFalse() {
        TodoItem todoItem = new TodoItem().setId("item-id").setTask("Item Task").setDone(false);
        RapidApiPrincipal principal = new RapidApiPrincipal("proxy-secret", "user", Subscription.BASIC);
        Mockito.when(todoItemService.update(eq(principal), eq("list-id"), eq(todoItem))).thenReturn(false);

        Entity<TodoItem> entity = Entity.entity(todoItem, MediaType.APPLICATION_JSON_TYPE);
        Response response = target("/v1/lists/list-id/items/item-id").request()
                .header(SecurityHeader.RAPID_API_PROXY_SECRET.getHeader(), "proxy-secret")
                .header(SecurityHeader.RAPID_API_USER.getHeader(), "user")
                .header(SecurityHeader.RAPID_API_SUBSCRIPTION.getHeader(), Subscription.BASIC.name())
                .put(entity);
        verifyErrorResponse(response, Response.Status.NOT_FOUND.getStatusCode(),
                "Item with id item-id not found in list with id list-id");
        Mockito.verify(todoItemService, Mockito.times(1)).update(eq(principal), eq("list-id"), eq(todoItem));
    }

    @Test
    public void testTodoItemUpdateTrue() {
        TodoItem todoItem = new TodoItem().setId("item-id").setTask("Item Task").setDone(false);
        RapidApiPrincipal principal = new RapidApiPrincipal("proxy-secret", "user", Subscription.BASIC);
        Mockito.when(todoItemService.update(eq(principal), eq("list-id"), eq(todoItem))).thenReturn(true);

        Entity<TodoItem> entity = Entity.entity(todoItem, MediaType.APPLICATION_JSON_TYPE);
        Response response = target("/v1/lists/list-id/items/item-id").request()
                .header(SecurityHeader.RAPID_API_PROXY_SECRET.getHeader(), "proxy-secret")
                .header(SecurityHeader.RAPID_API_USER.getHeader(), "user")
                .header(SecurityHeader.RAPID_API_SUBSCRIPTION.getHeader(), Subscription.BASIC.name())
                .put(entity);

        Assertions.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        Assertions.assertEquals(MediaType.APPLICATION_JSON_TYPE, response.getMediaType());
        Assertions.assertEquals(todoItem, response.readEntity(TodoItem.class));
        verifyCorsHeaders(response);
        Mockito.verify(todoItemService, Mockito.times(1)).update(eq(principal), eq("list-id"), eq(todoItem));
    }

    @Test
    public void testTodoItemNoId() {
        RapidApiPrincipal principal = new RapidApiPrincipal("proxy-secret", "user", Subscription.BASIC);
        Mockito.when(todoItemService.update(eq(principal), eq("list-id"), any())).thenReturn(true);

        TodoItem todoItem = new TodoItem().setTask("Item Task").setDone(false);
        Entity<TodoItem> entity = Entity.entity(todoItem, MediaType.APPLICATION_JSON_TYPE);
        Response response = target("/v1/lists/list-id/items/item-id").request()
                .header(SecurityHeader.RAPID_API_PROXY_SECRET.getHeader(), "proxy-secret")
                .header(SecurityHeader.RAPID_API_USER.getHeader(), "user")
                .header(SecurityHeader.RAPID_API_SUBSCRIPTION.getHeader(), Subscription.BASIC.name())
                .put(entity);

        Assertions.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        Assertions.assertEquals(MediaType.APPLICATION_JSON_TYPE, response.getMediaType());
        TodoItem result = response.readEntity(TodoItem.class);
        todoItem.setId("item-id");
        Assertions.assertEquals(todoItem, result);
        verifyCorsHeaders(response);
        Mockito.verify(todoItemService, Mockito.times(1)).update(eq(principal), eq("list-id"), eq(result));
    }

    @Test
    public void testServiceException() {
        TodoItem todoItem = new TodoItem().setId("item-id").setTask("Item Task").setDone(false);
        RapidApiPrincipal principal = new RapidApiPrincipal("proxy-secret", "user", Subscription.BASIC);
        Mockito.when(todoItemService.update(eq(principal), eq("list-id"), eq(todoItem)))
                .thenThrow(new RuntimeException("Failed"));

        Entity<TodoItem> entity = Entity.entity(todoItem, MediaType.APPLICATION_JSON_TYPE);
        Response response = target("/v1/lists/list-id/items/item-id").request()
                .header(SecurityHeader.RAPID_API_PROXY_SECRET.getHeader(), "proxy-secret")
                .header(SecurityHeader.RAPID_API_USER.getHeader(), "user")
                .header(SecurityHeader.RAPID_API_SUBSCRIPTION.getHeader(), Subscription.BASIC.name())
                .put(entity);
        verifyErrorResponse(response, Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), "Failed");
        Mockito.verify(todoItemService, Mockito.times(1)).update(eq(principal), eq("list-id"), eq(todoItem));
    }

    @Test
    public void testValidationException() {
        TodoItem todoItem = new TodoItem().setId("id").setTask(Strings.padEnd("Item Task", 201, 'a')).setDone(false);
        Entity<TodoItem> entity = Entity.entity(todoItem, MediaType.APPLICATION_JSON_TYPE);
        Response response = target("/v1/lists/list-id/items/item-id").request()
                .header(SecurityHeader.RAPID_API_PROXY_SECRET.getHeader(), "proxy-secret")
                .header(SecurityHeader.RAPID_API_USER.getHeader(), "user")
                .header(SecurityHeader.RAPID_API_SUBSCRIPTION.getHeader(), Subscription.BASIC.name())
                .put(entity);
        verifyErrorResponse(response, Response.Status.BAD_REQUEST.getStatusCode(),
                "Todo item task max length is 200 characters");
        Mockito.verify(todoItemService, Mockito.times(0)).update(any(), any(), any());
    }

    @Test
    public void testHtml() {
        TodoItem todoItem = new TodoItem().setId("item-id").setTask("<h1>Item & Task</h1>").setDone(false);
        RapidApiPrincipal principal = new RapidApiPrincipal("proxy-secret", "user", Subscription.BASIC);
        Mockito.when(todoItemService.update(eq(principal), eq("list-id"), any())).thenReturn(true);

        Entity<TodoItem> entity = Entity.entity(todoItem, MediaType.APPLICATION_JSON_TYPE);
        Response response = target("/v1/lists/list-id/items/item-id").request()
                .header(SecurityHeader.RAPID_API_PROXY_SECRET.getHeader(), "proxy-secret")
                .header(SecurityHeader.RAPID_API_USER.getHeader(), "user")
                .header(SecurityHeader.RAPID_API_SUBSCRIPTION.getHeader(), Subscription.BASIC.name())
                .put(entity);

        Assertions.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        Assertions.assertEquals(MediaType.APPLICATION_JSON_TYPE, response.getMediaType());
        TodoItem result = response.readEntity(TodoItem.class);
        Assertions.assertEquals("&lt;h1&gt;Item &amp; Task&lt;/h1&gt;", result.getTask());
        verifyCorsHeaders(response);
        Mockito.verify(todoItemService, Mockito.times(1)).update(eq(principal), eq("list-id"), any());
    }

    @Test
    public void testNullValues() {
        TodoItem todoItem = new TodoItem().setId(null).setTask(null).setDone(false);
        Entity<TodoItem> entity = Entity.entity(todoItem, MediaType.APPLICATION_JSON_TYPE);
        Response response = target("/v1/lists/list-id/items/item-id").request()
                .header(SecurityHeader.RAPID_API_PROXY_SECRET.getHeader(), "proxy-secret")
                .header(SecurityHeader.RAPID_API_USER.getHeader(), "user")
                .header(SecurityHeader.RAPID_API_SUBSCRIPTION.getHeader(), Subscription.BASIC.name())
                .put(entity);
        verifyErrorResponse(response, Response.Status.BAD_REQUEST.getStatusCode(), "Todo item task cannot be empty");
        Mockito.verify(todoItemService, Mockito.times(0)).update(any(), any(), any());
    }

    @Test
    public void testEmptyValues() {
        TodoItem todoItem = new TodoItem().setId("").setTask("").setDone(false);
        Entity<TodoItem> entity = Entity.entity(todoItem, MediaType.APPLICATION_JSON_TYPE);
        Response response = target("/v1/lists/list-id/items/item-id").request()
                .header(SecurityHeader.RAPID_API_PROXY_SECRET.getHeader(), "proxy-secret")
                .header(SecurityHeader.RAPID_API_USER.getHeader(), "user")
                .header(SecurityHeader.RAPID_API_SUBSCRIPTION.getHeader(), Subscription.BASIC.name())
                .put(entity);
        verifyErrorResponse(response, Response.Status.BAD_REQUEST.getStatusCode(), "Todo item task cannot be empty");
        Mockito.verify(todoItemService, Mockito.times(0)).update(any(), any(), any());
    }
}
