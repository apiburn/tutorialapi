package com.tutorialapi.rest.resource.v1.lists;

import com.tutorialapi.db.ServiceFactory;
import com.tutorialapi.model.TodoList;
import com.tutorialapi.model.user.RapidApiPrincipal;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.SecurityContext;

@Path("/v1/lists/{id}")
public class DeleteTodoListResource {
    private final ServiceFactory serviceFactory;

    @Inject
    public DeleteTodoListResource(ServiceFactory serviceFactory) {
        this.serviceFactory = serviceFactory;
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    public TodoList postTodoList(@Context SecurityContext securityContext, @PathParam("id") String listId) {
        RapidApiPrincipal principal = (RapidApiPrincipal) securityContext.getUserPrincipal();
        return serviceFactory.getTodoListService().delete(principal, listId)
                .orElseThrow(() -> new NotFoundException("Todo list with id " + listId + " not found"));
    }
}
