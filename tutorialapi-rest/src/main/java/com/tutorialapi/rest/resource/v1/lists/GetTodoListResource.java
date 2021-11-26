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
public class GetTodoListResource {
    private final ServiceFactory serviceFactory;

    @Inject
    public GetTodoListResource(ServiceFactory serviceFactory) {
        this.serviceFactory = serviceFactory;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public TodoList getTodoList(@Context SecurityContext securityContext, @PathParam("id") String listId) {
        RapidApiPrincipal principal = (RapidApiPrincipal) securityContext.getUserPrincipal();
        return serviceFactory.getTodoListService().get(principal, listId)
                .orElseThrow(() -> new NotFoundException("List with id " + listId + " not found"));
    }
}
