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

@Path("/v1/lists/{listId}")
public class PutTodoListResource extends BaseResource {
    private final ServiceFactory serviceFactory;

    @Inject
    public PutTodoListResource(ServiceFactory serviceFactory) {
        this.serviceFactory = serviceFactory;
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public TodoList putTodoList(@Context SecurityContext securityContext,
                                @PathParam("listId") String listId,
                                TodoList todoList) {
        todoList.setId(listId);
        todoList.setName(Optional.ofNullable(todoList.getName()).map(StringEscapeUtils::escapeHtml4).orElse(null));
        validate(todoList);

        RapidApiPrincipal principal = (RapidApiPrincipal) securityContext.getUserPrincipal();
        if (serviceFactory.getTodoListService().update(principal, todoList)) {
            return todoList;
        }
        throw new NotFoundException("List with id " + listId + " not found");
    }
}
