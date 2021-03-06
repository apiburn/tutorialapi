package com.tutorialapi.rest.resource.v1.items;

import com.google.common.base.Strings;
import com.tutorialapi.db.ServiceFactory;
import com.tutorialapi.db.exception.ConflictException;
import com.tutorialapi.db.service.TodoItemService;
import com.tutorialapi.db.service.TodoListService;
import com.tutorialapi.model.TodoItem;
import com.tutorialapi.model.TodoList;
import com.tutorialapi.model.config.ConfigKey;
import com.tutorialapi.model.user.RapidApiPrincipal;
import com.tutorialapi.model.user.Subscription;
import com.tutorialapi.rest.ApiApplication;
import com.tutorialapi.rest.Environment;
import com.tutorialapi.rest.resource.v1.BaseResourceIT;
import com.tutorialapi.rest.security.SecurityHeader;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;
import java.util.Properties;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

public class PostTodoItemResourceIT extends BaseResourceIT {
    private TodoListService todoListService;
    private TodoItemService todoItemService;

    @Override
    protected Application configure() {
        ServiceFactory serviceFactory = Mockito.mock(ServiceFactory.class);
        todoListService = Mockito.mock(TodoListService.class);
        todoItemService = Mockito.mock(TodoItemService.class);
        Mockito.when(serviceFactory.getTodoListService()).thenReturn(todoListService);
        Mockito.when(serviceFactory.getTodoItemService()).thenReturn(todoItemService);

        Properties configProperties = new Properties();
        configProperties.setProperty(ConfigKey.RAPIDAPI_PROXY_SECRET.getKey(), "proxy-secret");
        Config config = ConfigFactory.parseProperties(configProperties);

        return new ApiApplication(() -> new Environment(config, serviceFactory));
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
    public void testTodoListNotFound() {
        RapidApiPrincipal principal = new RapidApiPrincipal("proxy-secret", "user", Subscription.BASIC);
        Mockito.when(todoListService.get(eq(principal), eq("list-id"))).thenReturn(Optional.empty());
        TodoItem todoItem = new TodoItem().setId("item-id").setTask("Item Task").setDone(false);
        Entity<TodoItem> entity = Entity.entity(todoItem, MediaType.APPLICATION_JSON_TYPE);
        Response response = target("/v1/lists/list-id/items").request()
                .header(SecurityHeader.RAPID_API_PROXY_SECRET.getHeader(), "proxy-secret")
                .header(SecurityHeader.RAPID_API_USER.getHeader(), "user")
                .header(SecurityHeader.RAPID_API_SUBSCRIPTION.getHeader(), Subscription.BASIC.name())
                .post(entity);
        verifyErrorResponse(response, Response.Status.NOT_FOUND.getStatusCode(), "List with id list-id not found");
        Mockito.verify(todoListService, Mockito.times(1)).get(eq(principal), eq("list-id"));
        Mockito.verify(todoItemService, Mockito.times(0)).create(any(), any(), any());
    }

    @Test
    public void testTodoItemCreateFalse() {
        RapidApiPrincipal principal = new RapidApiPrincipal("proxy-secret", "user", Subscription.BASIC);
        TodoList todoList = new TodoList().setId("list-id").setName("List Name");
        Mockito.when(todoListService.get(eq(principal), eq("list-id"))).thenReturn(Optional.of(todoList));
        TodoItem todoItem = new TodoItem().setId("item-id").setTask("Item Task").setDone(false);
        Mockito.when(todoItemService.create(eq(principal), eq("list-id"), eq(todoItem))).thenReturn(false);

        Entity<TodoItem> entity = Entity.entity(todoItem, MediaType.APPLICATION_JSON_TYPE);
        Response response = target("/v1/lists/list-id/items").request()
                .header(SecurityHeader.RAPID_API_PROXY_SECRET.getHeader(), "proxy-secret")
                .header(SecurityHeader.RAPID_API_USER.getHeader(), "user")
                .header(SecurityHeader.RAPID_API_SUBSCRIPTION.getHeader(), Subscription.BASIC.name())
                .post(entity);
        verifyErrorResponse(response, Response.Status.BAD_REQUEST.getStatusCode(),
                "Invalid input, failed to insert todo item");
        Mockito.verify(todoListService, Mockito.times(1)).get(eq(principal), eq("list-id"));
        Mockito.verify(todoItemService, Mockito.times(1)).create(eq(principal), eq("list-id"), eq(todoItem));
    }

    @Test
    public void testTodoItemCreateTrue() {
        RapidApiPrincipal principal = new RapidApiPrincipal("proxy-secret", "user", Subscription.BASIC);
        TodoList todoList = new TodoList().setId("list-id").setName("List Name");
        Mockito.when(todoListService.get(eq(principal), eq("list-id"))).thenReturn(Optional.of(todoList));
        TodoItem todoItem = new TodoItem().setId("item-id").setTask("Item Task").setDone(false);
        Mockito.when(todoItemService.create(eq(principal), eq("list-id"), eq(todoItem))).thenReturn(true);

        Entity<TodoItem> entity = Entity.entity(todoItem, MediaType.APPLICATION_JSON_TYPE);
        Response response = target("/v1/lists/list-id/items").request()
                .header(SecurityHeader.RAPID_API_PROXY_SECRET.getHeader(), "proxy-secret")
                .header(SecurityHeader.RAPID_API_USER.getHeader(), "user")
                .header(SecurityHeader.RAPID_API_SUBSCRIPTION.getHeader(), Subscription.BASIC.name())
                .post(entity);

        Assertions.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        Assertions.assertEquals(MediaType.APPLICATION_JSON_TYPE, response.getMediaType());
        Assertions.assertEquals(todoItem, response.readEntity(TodoItem.class));
        verifyCorsHeaders(response);
        Mockito.verify(todoListService, Mockito.times(1)).get(eq(principal), eq("list-id"));
        Mockito.verify(todoItemService, Mockito.times(1)).create(eq(principal), eq("list-id"), eq(todoItem));
    }

    @Test
    public void testTodoItemNoId() {
        RapidApiPrincipal principal = new RapidApiPrincipal("proxy-secret", "user", Subscription.BASIC);
        TodoList todoList = new TodoList().setId("list-id").setName("List Name");
        Mockito.when(todoListService.get(eq(principal), eq("list-id"))).thenReturn(Optional.of(todoList));
        Mockito.when(todoItemService.create(eq(principal), eq("list-id"), any())).thenReturn(true);

        TodoItem todoItem = new TodoItem().setTask("Item Task").setDone(false);
        Entity<TodoItem> entity = Entity.entity(todoItem, MediaType.APPLICATION_JSON_TYPE);
        Response response = target("/v1/lists/list-id/items").request()
                .header(SecurityHeader.RAPID_API_PROXY_SECRET.getHeader(), "proxy-secret")
                .header(SecurityHeader.RAPID_API_USER.getHeader(), "user")
                .header(SecurityHeader.RAPID_API_SUBSCRIPTION.getHeader(), Subscription.BASIC.name())
                .post(entity);

        Assertions.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        Assertions.assertEquals(MediaType.APPLICATION_JSON_TYPE, response.getMediaType());
        TodoItem result = response.readEntity(TodoItem.class);
        Assertions.assertNotNull(result.getId());
        Assertions.assertEquals(todoItem.getTask(), result.getTask());
        Assertions.assertEquals(todoItem.isDone(), result.isDone());
        verifyCorsHeaders(response);
        Mockito.verify(todoListService, Mockito.times(1)).get(eq(principal), eq("list-id"));
        Mockito.verify(todoItemService, Mockito.times(1)).create(eq(principal), eq("list-id"), eq(result));
    }

    @Test
    public void testConflictException() {
        RapidApiPrincipal principal = new RapidApiPrincipal("proxy-secret", "user", Subscription.BASIC);
        TodoList todoList = new TodoList().setId("list-id").setName("List Name");
        Mockito.when(todoListService.get(eq(principal), eq("list-id"))).thenReturn(Optional.of(todoList));
        TodoItem todoItem = new TodoItem().setId("item-id").setTask("Item Task").setDone(false);
        Mockito.when(todoItemService.create(eq(principal), eq("list-id"), eq(todoItem)))
                .thenThrow(new ConflictException("Already exists"));

        Entity<TodoItem> entity = Entity.entity(todoItem, MediaType.APPLICATION_JSON_TYPE);
        Response response = target("/v1/lists/list-id/items").request()
                .header(SecurityHeader.RAPID_API_PROXY_SECRET.getHeader(), "proxy-secret")
                .header(SecurityHeader.RAPID_API_USER.getHeader(), "user")
                .header(SecurityHeader.RAPID_API_SUBSCRIPTION.getHeader(), Subscription.BASIC.name())
                .post(entity);
        verifyErrorResponse(response, Response.Status.CONFLICT.getStatusCode(), "Already exists");
        Mockito.verify(todoListService, Mockito.times(1)).get(eq(principal), eq("list-id"));
        Mockito.verify(todoItemService, Mockito.times(1)).create(eq(principal), eq("list-id"), eq(todoItem));
    }

    @Test
    public void testServiceException() {
        RapidApiPrincipal principal = new RapidApiPrincipal("proxy-secret", "user", Subscription.BASIC);
        TodoList todoList = new TodoList().setId("list-id").setName("List Name");
        Mockito.when(todoListService.get(eq(principal), eq("list-id"))).thenReturn(Optional.of(todoList));
        TodoItem todoItem = new TodoItem().setId("item-id").setTask("Item Task").setDone(false);
        Mockito.when(todoItemService.create(eq(principal), eq("list-id"), eq(todoItem)))
                .thenThrow(new RuntimeException("Failed"));

        Entity<TodoItem> entity = Entity.entity(todoItem, MediaType.APPLICATION_JSON_TYPE);
        Response response = target("/v1/lists/list-id/items").request()
                .header(SecurityHeader.RAPID_API_PROXY_SECRET.getHeader(), "proxy-secret")
                .header(SecurityHeader.RAPID_API_USER.getHeader(), "user")
                .header(SecurityHeader.RAPID_API_SUBSCRIPTION.getHeader(), Subscription.BASIC.name())
                .post(entity);
        verifyErrorResponse(response, Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), "Failed");
        Mockito.verify(todoListService, Mockito.times(1)).get(eq(principal), eq("list-id"));
        Mockito.verify(todoItemService, Mockito.times(1)).create(eq(principal), eq("list-id"), eq(todoItem));
    }

    @Test
    public void testValidationException() {
        TodoItem todoItem = new TodoItem().setId("id").setTask(Strings.padEnd("Item Task", 201, 'a')).setDone(false);
        Entity<TodoItem> entity = Entity.entity(todoItem, MediaType.APPLICATION_JSON_TYPE);
        Response response = target("/v1/lists/list-id/items").request()
                .header(SecurityHeader.RAPID_API_PROXY_SECRET.getHeader(), "proxy-secret")
                .header(SecurityHeader.RAPID_API_USER.getHeader(), "user")
                .header(SecurityHeader.RAPID_API_SUBSCRIPTION.getHeader(), Subscription.BASIC.name())
                .post(entity);
        verifyErrorResponse(response, Response.Status.BAD_REQUEST.getStatusCode(),
                "Todo item task must have at least 1 and no more than 200 characters");
        Mockito.verify(todoItemService, Mockito.times(0)).create(any(), any(), any());
    }

    @Test
    public void testHtml() {
        RapidApiPrincipal principal = new RapidApiPrincipal("proxy-secret", "user", Subscription.BASIC);
        TodoList todoList = new TodoList().setId("list-id").setName("List Name");
        Mockito.when(todoListService.get(eq(principal), eq("list-id"))).thenReturn(Optional.of(todoList));
        TodoItem todoItem = new TodoItem().setId("item-id").setTask("<h1>Item & Task</h1>").setDone(false);
        Mockito.when(todoItemService.create(eq(principal), eq("list-id"), any())).thenReturn(true);

        Entity<TodoItem> entity = Entity.entity(todoItem, MediaType.APPLICATION_JSON_TYPE);
        Response response = target("/v1/lists/list-id/items").request()
                .header(SecurityHeader.RAPID_API_PROXY_SECRET.getHeader(), "proxy-secret")
                .header(SecurityHeader.RAPID_API_USER.getHeader(), "user")
                .header(SecurityHeader.RAPID_API_SUBSCRIPTION.getHeader(), Subscription.BASIC.name())
                .post(entity);

        Assertions.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        Assertions.assertEquals(MediaType.APPLICATION_JSON_TYPE, response.getMediaType());
        TodoItem result = response.readEntity(TodoItem.class);
        Assertions.assertEquals("&lt;h1&gt;Item &amp; Task&lt;/h1&gt;", result.getTask());
        verifyCorsHeaders(response);
        Mockito.verify(todoListService, Mockito.times(1)).get(eq(principal), eq("list-id"));
        Mockito.verify(todoItemService, Mockito.times(1)).create(eq(principal), eq("list-id"), any());
    }

    @Test
    public void testNullValues() {
        TodoItem todoItem = new TodoItem().setId(null).setTask(null).setDone(false);
        Entity<TodoItem> entity = Entity.entity(todoItem, MediaType.APPLICATION_JSON_TYPE);
        Response response = target("/v1/lists/list-id/items").request()
                .header(SecurityHeader.RAPID_API_PROXY_SECRET.getHeader(), "proxy-secret")
                .header(SecurityHeader.RAPID_API_USER.getHeader(), "user")
                .header(SecurityHeader.RAPID_API_SUBSCRIPTION.getHeader(), Subscription.BASIC.name())
                .post(entity);
        verifyErrorResponse(response, Response.Status.BAD_REQUEST.getStatusCode(), "Todo item task cannot be empty");
        Mockito.verify(todoItemService, Mockito.times(0)).create(any(), any(), any());
    }

    @Test
    public void testEmptyValues() {
        TodoItem todoItem = new TodoItem().setId("").setTask("").setDone(false);
        Entity<TodoItem> entity = Entity.entity(todoItem, MediaType.APPLICATION_JSON_TYPE);
        Response response = target("/v1/lists/list-id/items").request()
                .header(SecurityHeader.RAPID_API_PROXY_SECRET.getHeader(), "proxy-secret")
                .header(SecurityHeader.RAPID_API_USER.getHeader(), "user")
                .header(SecurityHeader.RAPID_API_SUBSCRIPTION.getHeader(), Subscription.BASIC.name())
                .post(entity);
        verifyErrorResponse(response, Response.Status.BAD_REQUEST.getStatusCode(),
                "Todo item id cannot be empty; Todo item id must have at least 1 and no more than 36 characters; " +
                "Todo item task cannot be empty; Todo item task must have at least 1 and no more than 200 characters");
        Mockito.verify(todoItemService, Mockito.times(0)).create(any(), any(), any());
    }
}
