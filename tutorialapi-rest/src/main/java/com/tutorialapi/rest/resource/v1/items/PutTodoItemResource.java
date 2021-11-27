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

@Path("/v1/lists/{listId}/items/{id}")
public class PutTodoItemResource extends BaseResource {
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
        todoItem.setTask(Optional.ofNullable(todoItem.getTask()).map(StringEscapeUtils::escapeHtml4).orElse(null));
        validate(todoItem);

        RapidApiPrincipal principal = (RapidApiPrincipal) securityContext.getUserPrincipal();
        if (serviceFactory.getTodoItemService().update(principal, listId, todoItem)) {
            return todoItem;
        }
        throw new NotFoundException("Item with id " + id + " not found in list with id " + listId);
    }
}
