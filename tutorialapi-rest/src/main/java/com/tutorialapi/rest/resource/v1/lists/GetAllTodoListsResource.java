package com.tutorialapi.rest.resource.v1.lists;

import com.tutorialapi.db.ServiceFactory;
import com.tutorialapi.model.TodoList;
import com.tutorialapi.model.user.RapidApiPrincipal;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.SecurityContext;

import java.util.List;

@Path("/v1/lists")
public class GetAllTodoListsResource {
    private final ServiceFactory serviceFactory;

    @Inject
    public GetAllTodoListsResource(ServiceFactory serviceFactory) {
        this.serviceFactory = serviceFactory;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<TodoList> getAllTodoLists(@Context SecurityContext securityContext) {
        RapidApiPrincipal principal = (RapidApiPrincipal) securityContext.getUserPrincipal();
        return serviceFactory.getTodoListService().getAll(principal);
    }
}
