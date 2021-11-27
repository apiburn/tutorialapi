package com.tutorialapi.rest.resource.v1.items;

import com.tutorialapi.db.ServiceFactory;
import com.tutorialapi.model.TodoItem;
import com.tutorialapi.model.user.RapidApiPrincipal;
import com.tutorialapi.rest.exception.ErrorResponse;
import com.tutorialapi.rest.resource.v1.BaseResource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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
    @Operation(
            tags = "Todo Items",
            summary = "Update Item",
            description = """
                    Update an existing `TodoItem` in the specified `TodoList`.

                    This example shows how to use `curl` to update an existing `TodoItem`:

                    ```bash
                    curl -sk https://localhost:8443/api/lists/list-id/items/item-id -XPUT \\
                         -H "X-RapidAPI-Proxy-Secret: secret" \\
                         -H "X-RapidAPI-User: user" \\
                         -H "X-RapidAPI-Subscription: BASIC" \\
                         -H "Content-Type: application/json" \\
                         -d '{"task": "Mow the lawn", "done": true}'
                    ```

                    After successfully updating a `TodoItem`, this endpoint will return the modified item:

                    ```json
                    {
                      "id": "item-id",
                      "task": "Mow the lawn",
                      "done": true
                    }
                    ```

                    If a list with the specified `listId` does not exist, then this endpoint will return a
                    `404 Not Found` response like this example:

                    ```json
                    {
                      "status": 404,
                      "message": "List with id list-id not found"
                    }
                    ```
                    """,
            operationId = "update-item",
            requestBody = @RequestBody(
                    description = """
                            The request body is expected to provide the updated `TodoItem`.
                            """,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            examples = @ExampleObject(
                                    name = "todo-item-body",
                                    summary = "Example TodoItem",
                                    description = """
                                            Provides an example `TodoItem` request body to update:
                                            """,
                                    value = """
                                            {
                                              "task": "Mow the lawn",
                                              "done": true
                                            }
                                            """
                            ),
                            schema = @Schema(implementation = TodoItem.class)
                    ),
                    required = true
            ),
            responses = {
                    @ApiResponse(
                            description = "Successful Update",
                            responseCode = "200",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON,
                                    examples = @ExampleObject(
                                            name = "success",
                                            summary = "Example TodoItem",
                                            description = """
                                                    Provides an example `TodoItem` returned after successfully
                                                    updating the existing item in the database:
                                                    """,
                                            value = """
                                                    {
                                                      "id": "item-id",
                                                      "task": "Mow the lawn",
                                                      "done": true
                                                    }
                                                    """
                                    ),
                                    schema = @Schema(implementation = TodoItem.class)
                            )
                    ),
                    @ApiResponse(
                            description = "Bad Request",
                            responseCode = "400",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON,
                                    examples = @ExampleObject(
                                            name = "bad-request",
                                            summary = "Example ErrorResponse",
                                            description = """
                                                    Provides an example `ErrorResponse` returned when the provided
                                                    item is invalid:
                                                    """,
                                            value = """
                                                    {
                                                      "status": 400,
                                                      "message": "Todo item task cannot be empty"
                                                    }
                                                    """
                                    ),
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    ),
                    @ApiResponse(
                            description = "Not Found",
                            responseCode = "404",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON,
                                    examples = @ExampleObject(
                                            name = "not-found",
                                            summary = "Example ErrorResponse",
                                            description = """
                                                    Provides an example `ErrorResponse` returned when the specified
                                                    list is not found:
                                                    """,
                                            value = """
                                                    {
                                                      "status": 404,
                                                      "message": "List with id list-id not found"
                                                    }
                                                    """
                                    ),
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    ),
                    @ApiResponse(
                            description = "Server Error",
                            responseCode = "500",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON,
                                    examples = @ExampleObject(
                                            name = "server-error",
                                            summary = "Example ErrorResponse",
                                            value = """
                                                    {
                                                      "status": 500,
                                                      "message": "The database is offline"
                                                    }
                                                    """,
                                            description = """
                                                    Provides an example ErrorResponse returned when there is an
                                                    unexpected server error.
                                                    """
                                    ),
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    )
            }
    )
    public TodoItem putTodoItem(@Context SecurityContext securityContext,
                                @Parameter(
                                        name = "listId",
                                        in = ParameterIn.PATH,
                                        description = "The unique id of the list containing the item to update",
                                        required = true,
                                        example = "my-list-id"
                                )
                                @PathParam("listId") String listId,
                                @Parameter(
                                        name = "id",
                                        in = ParameterIn.PATH,
                                        description = "The unique id of the item to update",
                                        required = true,
                                        example = "my-item-id"
                                )
                                @PathParam("id") String id,
                                TodoItem todoItem) {
        todoItem.setId(id);
        todoItem.setTask(Optional.ofNullable(todoItem.getTask()).map(StringEscapeUtils::escapeHtml4).orElse(null));
        validate(todoItem);

        RapidApiPrincipal principal = (RapidApiPrincipal) securityContext.getUserPrincipal();
        serviceFactory.getTodoListService().get(principal, listId)
                .orElseThrow(() -> new NotFoundException("List with id " + listId + " not found"));

        if (serviceFactory.getTodoItemService().update(principal, listId, todoItem)) {
            return todoItem;
        }
        throw new NotFoundException("Item with id " + id + " not found in list with id " + listId);
    }
}
