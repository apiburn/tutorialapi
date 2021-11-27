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

@Path("/v1/lists/{listId}/items/{id}")
public class GetTodoItemResource extends BaseResource {
    private final ServiceFactory serviceFactory;

    @Inject
    public GetTodoItemResource(ServiceFactory serviceFactory) {
        this.serviceFactory = serviceFactory;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public TodoItem getTodoItem(@Context SecurityContext securityContext,
                                @PathParam("listId") String listId,
                                @PathParam("id") String id) {
        RapidApiPrincipal principal = (RapidApiPrincipal) securityContext.getUserPrincipal();
        return serviceFactory.getTodoItemService().get(principal, listId, id)
                .orElseThrow(() -> new NotFoundException("Item with id " + id + " not found in list with id " + listId));
    }
}
