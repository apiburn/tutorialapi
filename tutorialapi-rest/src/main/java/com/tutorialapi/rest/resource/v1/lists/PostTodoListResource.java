package com.tutorialapi.rest.resource.v1.lists;

import com.tutorialapi.db.ServiceFactory;
import com.tutorialapi.model.TodoList;
import com.tutorialapi.model.user.RapidApiPrincipal;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.SecurityContext;

import java.util.Optional;
import java.util.UUID;

@Path("/v1/lists")
public class PostTodoListResource {
    private final ServiceFactory serviceFactory;

    @Inject
    public PostTodoListResource(ServiceFactory serviceFactory) {
        this.serviceFactory = serviceFactory;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public TodoList postTodoList(@Context SecurityContext securityContext, TodoList todoList) {
        todoList.setId(Optional.ofNullable(todoList.getId()).orElseGet(() -> UUID.randomUUID().toString()));

        RapidApiPrincipal principal = (RapidApiPrincipal) securityContext.getUserPrincipal();
        if (serviceFactory.getTodoListService().create(principal, todoList)) {
            return todoList;
        }
        throw new BadRequestException("Invalid input, failed to insert todo list");
    }
}
