package com.tutorialapi.rest.resource.v1.items;

import com.tutorialapi.db.ServiceFactory;
import com.tutorialapi.model.TodoItem;
import com.tutorialapi.model.user.RapidApiPrincipal;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.SecurityContext;

@Path("/v1/lists/{listId}/items/{id}")
public class PutTodoItemResource {
    private final ServiceFactory serviceFactory;

    @Inject
    public PutTodoItemResource(ServiceFactory serviceFactory) {
        this.serviceFactory = serviceFactory;
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public TodoItem putTodoItem(@Context SecurityContext securityContext,
                                @PathParam("listId") String listId,
                                @PathParam("id") String id,
                                TodoItem todoItem) {
        todoItem.setId(id);

        RapidApiPrincipal principal = (RapidApiPrincipal) securityContext.getUserPrincipal();
        if (serviceFactory.getTodoItemService().update(principal, listId, todoItem)) {
            return todoItem;
        }
        throw new NotFoundException("Item with id " + id + " not found in list with id " + listId);
    }
}
