package com.tutorialapi.rest.resource.v1.lists;

import com.tutorialapi.db.ServiceFactory;
import com.tutorialapi.db.service.TodoListService;
import com.tutorialapi.model.TodoList;
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

public class DeleteTodoListResourceIT extends BaseResourceIT {
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
        Response response = target("/v1/lists/list-id").request().delete();
        verifyErrorResponse(response, Response.Status.UNAUTHORIZED.getStatusCode(),
                "Missing security header: X-RapidAPI-Proxy-Secret");
        Mockito.verify(todoListService, Mockito.times(0)).delete(any(), any());
    }

    @Test
    public void testOnlyProxySecretHeader() {
        Response response = target("/v1/lists/list-id").request()
                .header(SecurityHeader.RAPID_API_PROXY_SECRET.getHeader(), "proxy-secret")
                .delete();
        verifyErrorResponse(response, Response.Status.UNAUTHORIZED.getStatusCode(),
                "Missing security header: X-RapidAPI-User");
        Mockito.verify(todoListService, Mockito.times(0)).delete(any(), any());
    }

    @Test
    public void testProxySecretAndUserHeader() {
        Response response = target("/v1/lists/list-id").request()
                .header(SecurityHeader.RAPID_API_PROXY_SECRET.getHeader(), "proxy-secret")
                .header(SecurityHeader.RAPID_API_USER.getHeader(), "user")
                .delete();
        verifyErrorResponse(response, Response.Status.UNAUTHORIZED.getStatusCode(),
                "Missing or invalid security header: X-RapidAPI-Subscription");
        Mockito.verify(todoListService, Mockito.times(0)).delete(any(), any());
    }

    @Test
    public void testInvalidSubscription() {
        Response response = target("/v1/lists/list-id").request()
                .header(SecurityHeader.RAPID_API_PROXY_SECRET.getHeader(), "proxy-secret")
                .header(SecurityHeader.RAPID_API_USER.getHeader(), "user")
                .header(SecurityHeader.RAPID_API_SUBSCRIPTION.getHeader(), "invalid")
                .delete();
        verifyErrorResponse(response, Response.Status.UNAUTHORIZED.getStatusCode(),
                "Missing or invalid security header: X-RapidAPI-Subscription");
        Mockito.verify(todoListService, Mockito.times(0)).delete(any(), any());
    }

    @Test
    public void testMissingTodoList() {
        RapidApiPrincipal principal = new RapidApiPrincipal("proxy-secret", "user", Subscription.BASIC);
        Mockito.when(todoListService.delete(eq(principal), eq("list-id"))).thenReturn(Optional.empty());

        Response response = target("/v1/lists/list-id").request()
                .header(SecurityHeader.RAPID_API_PROXY_SECRET.getHeader(), "proxy-secret")
                .header(SecurityHeader.RAPID_API_USER.getHeader(), "user")
                .header(SecurityHeader.RAPID_API_SUBSCRIPTION.getHeader(), Subscription.BASIC.name())
                .delete();
        verifyErrorResponse(response, Response.Status.NOT_FOUND.getStatusCode(), "List with id list-id not found");
        Mockito.verify(todoListService, Mockito.times(1)).delete(eq(principal), eq("list-id"));
    }

    @Test
    public void testTodoListExists() {
        TodoList todoList = new TodoList().setId("list-id").setName("List Name");
        RapidApiPrincipal principal = new RapidApiPrincipal("proxy-secret", "user", Subscription.BASIC);
        Mockito.when(todoListService.delete(eq(principal), eq("list-id"))).thenReturn(Optional.of(todoList));

        Response response = target("/v1/lists/list-id").request()
                .header(SecurityHeader.RAPID_API_PROXY_SECRET.getHeader(), "proxy-secret")
                .header(SecurityHeader.RAPID_API_USER.getHeader(), "user")
                .header(SecurityHeader.RAPID_API_SUBSCRIPTION.getHeader(), Subscription.BASIC.name())
                .delete();

        Assertions.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        Assertions.assertEquals(MediaType.APPLICATION_JSON_TYPE, response.getMediaType());
        Assertions.assertEquals(todoList, response.readEntity(TodoList.class));
        verifyCorsHeaders(response);
        Mockito.verify(todoListService, Mockito.times(1)).delete(eq(principal), eq("list-id"));
    }

    @Test
    public void testServiceException() {
        RapidApiPrincipal principal = new RapidApiPrincipal("proxy-secret", "user", Subscription.BASIC);
        Mockito.when(todoListService.delete(eq(principal), eq("list-id"))).thenThrow(new RuntimeException("Failed"));

        Response response = target("/v1/lists/list-id").request()
                .header(SecurityHeader.RAPID_API_PROXY_SECRET.getHeader(), "proxy-secret")
                .header(SecurityHeader.RAPID_API_USER.getHeader(), "user")
                .header(SecurityHeader.RAPID_API_SUBSCRIPTION.getHeader(), Subscription.BASIC.name())
                .delete();
        verifyErrorResponse(response, Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), "Failed");
        Mockito.verify(todoListService, Mockito.times(1)).delete(eq(principal), eq("list-id"));
    }
}
