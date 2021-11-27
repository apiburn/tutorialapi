package com.tutorialapi.rest.resource.v1.items;

import com.tutorialapi.db.ServiceFactory;
import com.tutorialapi.model.TodoItem;
import com.tutorialapi.model.user.RapidApiPrincipal;
import com.tutorialapi.rest.resource.v1.BaseResource;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.SecurityContext;

import java.util.List;

@Path("/v1/lists/{listId}/items")
public class GetAllTodoItemsResource extends BaseResource {
    private final ServiceFactory serviceFactory;

    @Inject
    public GetAllTodoItemsResource(ServiceFactory serviceFactory) {
        this.serviceFactory = serviceFactory;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<TodoItem> getTodoItem(@Context SecurityContext securityContext, @PathParam("listId") String listId) {
        RapidApiPrincipal principal = (RapidApiPrincipal) securityContext.getUserPrincipal();
        return serviceFactory.getTodoItemService().getAll(principal, listId);
    }
}
