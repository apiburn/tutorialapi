package com.tutorialapi.rest.resource.v1.lists;

import com.tutorialapi.db.ServiceFactory;
import com.tutorialapi.model.TodoList;
import com.tutorialapi.model.user.RapidApiPrincipal;
import com.tutorialapi.rest.resource.v1.BaseResource;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.SecurityContext;
import org.apache.commons.text.StringEscapeUtils;

import java.util.Optional;
import java.util.UUID;

@Path("/v1/lists")
public class PostTodoListResource extends BaseResource {
    private final ServiceFactory serviceFactory;

    @Inject
    public PostTodoListResource(ServiceFactory serviceFactory) {
        this.serviceFactory = serviceFactory;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public TodoList postTodoList(@Context SecurityContext securityContext, TodoList todoList) {
        todoList.setId(Optional.ofNullable(todoList.getId())
                .map(StringEscapeUtils::escapeHtml4)
                .orElseGet(() -> UUID.randomUUID().toString()));
        todoList.setName(Optional.ofNullable(todoList.getName()).map(StringEscapeUtils::escapeHtml4).orElse(null));
        validate(todoList);

        RapidApiPrincipal principal = (RapidApiPrincipal) securityContext.getUserPrincipal();
        if (serviceFactory.getTodoListService().create(principal, todoList)) {
            return todoList;
        }
        throw new BadRequestException("Invalid input, failed to insert todo list");
    }
}
