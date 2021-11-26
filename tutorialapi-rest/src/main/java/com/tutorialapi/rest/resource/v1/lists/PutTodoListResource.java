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
public class PutTodoListResource {
    private final ServiceFactory serviceFactory;

    @Inject
    public PutTodoListResource(ServiceFactory serviceFactory) {
        this.serviceFactory = serviceFactory;
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public TodoList postTodoList(@Context SecurityContext securityContext,
                                 @PathParam("id") String listId,
                                 TodoList todoList) {
        todoList.setId(listId);

        RapidApiPrincipal principal = (RapidApiPrincipal) securityContext.getUserPrincipal();
        if (serviceFactory.getTodoListService().update(principal, todoList)) {
            return todoList;
        }
        throw new NotFoundException("Todo list with id " + listId + " not found");
    }
}
