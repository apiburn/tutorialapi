package com.tutorialapi.rest.resource.v1.lists;

import com.google.common.base.Strings;
import com.tutorialapi.db.ServiceFactory;
import com.tutorialapi.db.service.TodoListService;
import com.tutorialapi.model.TodoList;
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

public class PutTodoListResourceIT extends BaseResourceIT {
    private TodoListService todoListService;

    @Override
    protected Application configure() {
        ServiceFactory serviceFactory = Mockito.mock(ServiceFactory.class);
        todoListService = Mockito.mock(TodoListService.class);
        Mockito.when(serviceFactory.getTodoListService()).thenReturn(todoListService);

        return new ApiApplication(serviceFactory);
    }

    @Test
    public void testNoSecurityHeaders() {
        TodoList todoList = new TodoList().setId("list-id").setName("List Name");
        Entity<TodoList> entity = Entity.entity(todoList, MediaType.APPLICATION_JSON_TYPE);
        Response response = target("/v1/lists/list-id").request().put(entity);
        verifyErrorResponse(response, Response.Status.UNAUTHORIZED.getStatusCode(),
                "Missing security header: X-RapidAPI-Proxy-Secret");
        Mockito.verify(todoListService, Mockito.times(0)).update(any(), any());
    }

    @Test
    public void testOnlyProxySecretHeader() {
        TodoList todoList = new TodoList().setId("list-id").setName("List Name");
        Entity<TodoList> entity = Entity.entity(todoList, MediaType.APPLICATION_JSON_TYPE);
        Response response = target("/v1/lists/list-id").request()
                .header(SecurityHeader.RAPID_API_PROXY_SECRET.getHeader(), "proxy-secret")
                .put(entity);
        verifyErrorResponse(response, Response.Status.UNAUTHORIZED.getStatusCode(),
                "Missing security header: X-RapidAPI-User");
        Mockito.verify(todoListService, Mockito.times(0)).update(any(), any());
    }

    @Test
    public void testProxySecretAndUserHeader() {
        TodoList todoList = new TodoList().setId("list-id").setName("List Name");
        Entity<TodoList> entity = Entity.entity(todoList, MediaType.APPLICATION_JSON_TYPE);
        Response response = target("/v1/lists/list-id").request()
                .header(SecurityHeader.RAPID_API_PROXY_SECRET.getHeader(), "proxy-secret")
                .header(SecurityHeader.RAPID_API_USER.getHeader(), "user")
                .put(entity);
        verifyErrorResponse(response, Response.Status.UNAUTHORIZED.getStatusCode(),
                "Missing or invalid security header: X-RapidAPI-Subscription");
        Mockito.verify(todoListService, Mockito.times(0)).update(any(), any());
    }

    @Test
    public void testInvalidSubscription() {
        TodoList todoList = new TodoList().setId("list-id").setName("List Name");
        Entity<TodoList> entity = Entity.entity(todoList, MediaType.APPLICATION_JSON_TYPE);
        Response response = target("/v1/lists/list-id").request()
                .header(SecurityHeader.RAPID_API_PROXY_SECRET.getHeader(), "proxy-secret")
                .header(SecurityHeader.RAPID_API_USER.getHeader(), "user")
                .header(SecurityHeader.RAPID_API_SUBSCRIPTION.getHeader(), "invalid")
                .put(entity);
        verifyErrorResponse(response, Response.Status.UNAUTHORIZED.getStatusCode(),
                "Missing or invalid security header: X-RapidAPI-Subscription");
        Mockito.verify(todoListService, Mockito.times(0)).update(any(), any());
    }

    @Test
    public void testMissingTodoList() {
        TodoList todoList = new TodoList().setId("list-id").setName("List Name");
        RapidApiPrincipal principal = new RapidApiPrincipal("proxy-secret", "user", Subscription.BASIC);
        Mockito.when(todoListService.update(eq(principal), eq(todoList))).thenReturn(false);

        Entity<TodoList> entity = Entity.entity(todoList, MediaType.APPLICATION_JSON_TYPE);
        Response response = target("/v1/lists/list-id").request()
                .header(SecurityHeader.RAPID_API_PROXY_SECRET.getHeader(), "proxy-secret")
                .header(SecurityHeader.RAPID_API_USER.getHeader(), "user")
                .header(SecurityHeader.RAPID_API_SUBSCRIPTION.getHeader(), Subscription.BASIC.name())
                .put(entity);
        verifyErrorResponse(response, Response.Status.NOT_FOUND.getStatusCode(), "List with id list-id not found");
        Mockito.verify(todoListService, Mockito.times(1)).update(eq(principal), eq(todoList));
    }

    @Test
    public void testTodoListWrongId() {
        TodoList wrong = new TodoList().setId("wrong-id").setName("List Name");
        TodoList correct = new TodoList().setId("list-id").setName("List Name");
        RapidApiPrincipal principal = new RapidApiPrincipal("proxy-secret", "user", Subscription.BASIC);
        Mockito.when(todoListService.update(eq(principal), eq(correct))).thenReturn(true);

        Entity<TodoList> entity = Entity.entity(wrong, MediaType.APPLICATION_JSON_TYPE);
        Response response = target("/v1/lists/list-id").request()
                .header(SecurityHeader.RAPID_API_PROXY_SECRET.getHeader(), "proxy-secret")
                .header(SecurityHeader.RAPID_API_USER.getHeader(), "user")
                .header(SecurityHeader.RAPID_API_SUBSCRIPTION.getHeader(), Subscription.BASIC.name())
                .put(entity);

        Assertions.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        Assertions.assertEquals(MediaType.APPLICATION_JSON_TYPE, response.getMediaType());
        Assertions.assertEquals(correct, response.readEntity(TodoList.class));
        verifyCorsHeaders(response);
        Mockito.verify(todoListService, Mockito.times(1)).update(eq(principal), eq(correct));
    }

    @Test
    public void testTodoListExists() {
        TodoList todoList = new TodoList().setId("list-id").setName("List Name");
        RapidApiPrincipal principal = new RapidApiPrincipal("proxy-secret", "user", Subscription.BASIC);
        Mockito.when(todoListService.update(eq(principal), eq(todoList))).thenReturn(true);

        Entity<TodoList> entity = Entity.entity(todoList, MediaType.APPLICATION_JSON_TYPE);
        Response response = target("/v1/lists/list-id").request()
                .header(SecurityHeader.RAPID_API_PROXY_SECRET.getHeader(), "proxy-secret")
                .header(SecurityHeader.RAPID_API_USER.getHeader(), "user")
                .header(SecurityHeader.RAPID_API_SUBSCRIPTION.getHeader(), Subscription.BASIC.name())
                .put(entity);

        Assertions.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        Assertions.assertEquals(MediaType.APPLICATION_JSON_TYPE, response.getMediaType());
        Assertions.assertEquals(todoList, response.readEntity(TodoList.class));
        verifyCorsHeaders(response);
        Mockito.verify(todoListService, Mockito.times(1)).update(eq(principal), eq(todoList));
    }

    @Test
    public void testServiceException() {
        TodoList todoList = new TodoList().setId("list-id").setName("List Name");
        RapidApiPrincipal principal = new RapidApiPrincipal("proxy-secret", "user", Subscription.BASIC);
        Mockito.when(todoListService.update(eq(principal), eq(todoList))).thenThrow(new RuntimeException("Failed"));

        Entity<TodoList> entity = Entity.entity(todoList, MediaType.APPLICATION_JSON_TYPE);
        Response response = target("/v1/lists/list-id").request()
                .header(SecurityHeader.RAPID_API_PROXY_SECRET.getHeader(), "proxy-secret")
                .header(SecurityHeader.RAPID_API_USER.getHeader(), "user")
                .header(SecurityHeader.RAPID_API_SUBSCRIPTION.getHeader(), Subscription.BASIC.name())
                .put(entity);
        verifyErrorResponse(response, Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), "Failed");
        Mockito.verify(todoListService, Mockito.times(1)).update(eq(principal), eq(todoList));
    }

    @Test
    public void testValidationException() {
        TodoList todoList = new TodoList().setId("list-id").setName(Strings.padEnd("List Name", 201, 'a'));
        Entity<TodoList> entity = Entity.entity(todoList, MediaType.APPLICATION_JSON_TYPE);
        Response response = target("/v1/lists/list-id").request()
                .header(SecurityHeader.RAPID_API_PROXY_SECRET.getHeader(), "proxy-secret")
                .header(SecurityHeader.RAPID_API_USER.getHeader(), "user")
                .header(SecurityHeader.RAPID_API_SUBSCRIPTION.getHeader(), Subscription.BASIC.name())
                .put(entity);
        verifyErrorResponse(response, Response.Status.BAD_REQUEST.getStatusCode(),
                "Todo list name max length is 200 characters");
        Mockito.verify(todoListService, Mockito.times(0)).update(any(), any());
    }

    @Test
    public void testHtml() {
        TodoList todoList = new TodoList().setId("list-id").setName("<h1>List & Name</h1>");
        RapidApiPrincipal principal = new RapidApiPrincipal("proxy-secret", "user", Subscription.BASIC);
        Mockito.when(todoListService.update(eq(principal), any())).thenReturn(true);

        Entity<TodoList> entity = Entity.entity(todoList, MediaType.APPLICATION_JSON_TYPE);
        Response response = target("/v1/lists/list-id").request()
                .header(SecurityHeader.RAPID_API_PROXY_SECRET.getHeader(), "proxy-secret")
                .header(SecurityHeader.RAPID_API_USER.getHeader(), "user")
                .header(SecurityHeader.RAPID_API_SUBSCRIPTION.getHeader(), Subscription.BASIC.name())
                .put(entity);

        Assertions.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        Assertions.assertEquals(MediaType.APPLICATION_JSON_TYPE, response.getMediaType());
        TodoList result = response.readEntity(TodoList.class);
        Assertions.assertEquals("&lt;h1&gt;List &amp; Name&lt;/h1&gt;", result.getName());
        verifyCorsHeaders(response);
        Mockito.verify(todoListService, Mockito.times(1)).update(eq(principal), any());
    }

    @Test
    public void testNullValues() {
        TodoList todoList = new TodoList().setId(null).setName(null);
        Entity<TodoList> entity = Entity.entity(todoList, MediaType.APPLICATION_JSON_TYPE);
        Response response = target("/v1/lists/list-id").request()
                .header(SecurityHeader.RAPID_API_PROXY_SECRET.getHeader(), "proxy-secret")
                .header(SecurityHeader.RAPID_API_USER.getHeader(), "user")
                .header(SecurityHeader.RAPID_API_SUBSCRIPTION.getHeader(), Subscription.BASIC.name())
                .put(entity);
        verifyErrorResponse(response, Response.Status.BAD_REQUEST.getStatusCode(), "Todo list name cannot be empty");
        Mockito.verify(todoListService, Mockito.times(0)).update(any(), any());
    }

    @Test
    public void testEmptyValues() {
        TodoList todoList = new TodoList().setId("").setName("");
        Entity<TodoList> entity = Entity.entity(todoList, MediaType.APPLICATION_JSON_TYPE);
        Response response = target("/v1/lists/list-id").request()
                .header(SecurityHeader.RAPID_API_PROXY_SECRET.getHeader(), "proxy-secret")
                .header(SecurityHeader.RAPID_API_USER.getHeader(), "user")
                .header(SecurityHeader.RAPID_API_SUBSCRIPTION.getHeader(), Subscription.BASIC.name())
                .put(entity);
        verifyErrorResponse(response, Response.Status.BAD_REQUEST.getStatusCode(), "Todo list name cannot be empty");
        Mockito.verify(todoListService, Mockito.times(0)).update(any(), any());
    }
}
