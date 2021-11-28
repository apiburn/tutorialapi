package com.tutorialapi.rest.resource.v1.items;

import com.tutorialapi.db.ServiceFactory;
import com.tutorialapi.model.TodoItem;
import com.tutorialapi.model.user.RapidApiPrincipal;
import com.tutorialapi.rest.Environment;
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
import java.util.UUID;
import java.util.function.Supplier;

@Path("/v1/lists/{listId}/items")
public class PostTodoItemResource extends BaseResource {
    private final Supplier<Environment> environmentSupplier;

    @Inject
    public PostTodoItemResource(Supplier<Environment> environmentSupplier) {
        this.environmentSupplier = environmentSupplier;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            tags = "Todo Items",
            summary = "Add Item",
            description = """
                    Add a new `TodoItem` into the specified `TodoList`.

                    This example shows how to use `curl` to create a `TodoItem`:

                    ```bash
                    curl -sk https://localhost:8443/api/lists/list-id/items -XPOST \\
                         -H "X-RapidAPI-Proxy-Secret: secret" \\
                         -H "X-RapidAPI-User: user" \\
                         -H "X-RapidAPI-Subscription: BASIC" \\
                         -H "Content-Type: application/json" \\
                         -d '{"task": "Mow the lawn", "done": false}'
                    ```

                    Providing an `id` in the `TodoItem` is optional. If not specified, a unique `id` will be assigned
                    to the new `TodoItem`.

                    After successfully creating a `TodoItem`, this endpoint will return the created item:

                    ```json
                    {
                      "id": "123e4567-e89b-12d3-a456-556642440000",
                      "task": "Mow the lawn",
                      "done": false
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
            operationId = "add-item",
            requestBody = @RequestBody(
                    description = """
                            The request body is expected to provide the new `TodoItem` to insert into the specified
                            list. The `id` field is optional, and will be populated if not specified.
                            """,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            examples = @ExampleObject(
                                    name = "todo-item-body",
                                    summary = "Example TodoItem",
                                    description = """
                                            Provides an example `TodoItem` request body to save into the specified list:
                                            """,
                                    value = """
                                            {
                                              "task": "Mow the lawn",
                                              "done": false
                                            }
                                            """
                            ),
                            schema = @Schema(implementation = TodoItem.class)
                    ),
                    required = true
            ),
            responses = {
                    @ApiResponse(
                            description = "Successful Add",
                            responseCode = "200",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON,
                                    examples = @ExampleObject(
                                            name = "success",
                                            summary = "Example TodoItem",
                                            description = """
                                                    Provides an example `TodoItem` returned after successfully
                                                    inserting the item into the list:
                                                    """,
                                            value = """
                                                    {
                                                      "id": "item-id",
                                                      "task": "Mow the lawn",
                                                      "done": false
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
                                                    Provides an example `ErrorResponse` returned when the requested
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
                            description = "Conflict",
                            responseCode = "409",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON,
                                    examples = @ExampleObject(
                                            name = "conflict",
                                            summary = "Example ErrorResponse",
                                            description = """
                                                    Provides an example `ErrorResponse` returned when a `TodoItem`
                                                    already exists with the specified `id`:
                                                    """,
                                            value = """
                                                    {
                                                      "status": 409,
                                                      "message": "Todo item already exists"
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
    public TodoItem postTodoItem(@Context SecurityContext securityContext,
                                 @Parameter(
                                         name = "listId",
                                         in = ParameterIn.PATH,
                                         description = """
                                                The unique id of the list into which the new item should be added
                                                """,
                                         required = true,
                                         example = "my-list-id"
                                 )
                                 @PathParam("listId") String listId,
                                 TodoItem todoItem) {
        todoItem.setId(Optional.ofNullable(todoItem.getId())
                .map(StringEscapeUtils::escapeHtml4)
                .orElseGet(() -> UUID.randomUUID().toString()));
        todoItem.setTask(Optional.ofNullable(todoItem.getTask()).map(StringEscapeUtils::escapeHtml4).orElse(null));
        validate(todoItem);

        RapidApiPrincipal principal = (RapidApiPrincipal) securityContext.getUserPrincipal();
        ServiceFactory serviceFactory = environmentSupplier.get().getServiceFactory();
        serviceFactory.getTodoListService().get(principal, listId)
                .orElseThrow(() -> new NotFoundException("List with id " + listId + " not found"));

        if (serviceFactory.getTodoItemService().create(principal, listId, todoItem)) {
            return todoItem;
        }
        throw new BadRequestException("Invalid input, failed to insert todo item");
    }
}
