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
import org.apache.commons.text.StringEscapeUtils;

import java.util.Optional;
import java.util.UUID;

@Path("/v1/lists/{listId}/items")
public class PostTodoItemResource extends BaseResource {
    private final ServiceFactory serviceFactory;

    @Inject
    public PostTodoItemResource(ServiceFactory serviceFactory) {
        this.serviceFactory = serviceFactory;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public TodoItem postTodoItem(@Context SecurityContext securityContext,
                                 @PathParam("listId") String listId,
                                 TodoItem todoItem) {
        todoItem.setId(Optional.ofNullable(todoItem.getId())
                .map(StringEscapeUtils::escapeHtml4)
                .orElseGet(() -> UUID.randomUUID().toString()));
        todoItem.setTask(Optional.ofNullable(todoItem.getTask()).map(StringEscapeUtils::escapeHtml4).orElse(null));
        validate(todoItem);

        RapidApiPrincipal principal = (RapidApiPrincipal) securityContext.getUserPrincipal();
        if (serviceFactory.getTodoItemService().create(principal, listId, todoItem)) {
            return todoItem;
        }
        throw new BadRequestException("Invalid input, failed to insert todo item");
    }
}
