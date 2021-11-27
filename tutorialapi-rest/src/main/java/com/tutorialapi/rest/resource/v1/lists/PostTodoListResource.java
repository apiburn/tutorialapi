package com.tutorialapi.rest.resource.v1.lists;

import com.tutorialapi.db.ServiceFactory;
import com.tutorialapi.model.TodoList;
import com.tutorialapi.model.user.RapidApiPrincipal;
import com.tutorialapi.rest.exception.ErrorResponse;
import com.tutorialapi.rest.resource.v1.BaseResource;
import io.swagger.v3.oas.annotations.Operation;
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
    @Operation(
            tags = "Todo Lists",
            summary = "Add List",
            description = """
                    Add a new `TodoList`.

                    This example shows how to use `curl` to create a `TodoList`:

                    ```bash
                    curl -sk https://localhost:8443/api/lists -XPOST \\
                         -H "X-RapidAPI-Proxy-Secret: secret" \\
                         -H "X-RapidAPI-User: user" \\
                         -H "X-RapidAPI-Subscription: BASIC" \\
                         -H "Content-Type: application/json" \\
                         -d '{"name": "Spring cleaning tasks"}'
                    ```

                    Providing an `id` in the `TodoList` is optional. If not specified, a unique `id` will be assigned
                    to the new `TodoList`.

                    After successfully creating a `TodoList`, this endpoint will return the created list:

                    ```json
                    {
                      "id": "123e4567-e89b-12d3-a456-556642440000",
                      "name": "Spring cleaning tasks"
                    }
                    ```
                    """,
            operationId = "add-list",
            requestBody = @RequestBody(
                    description = """
                            The request body is expected to provide the new `TodoList`. The `id` field is optional, and
                            will be populated if not specified.
                            """,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            examples = @ExampleObject(
                                    name = "todo-list-body",
                                    summary = "Example TodoList",
                                    description = """
                                            Provides an example `TodoList` request body to save:
                                            """,
                                    value = """
                                            {
                                              "name": "Spring cleaning tasks"
                                            }
                                            """
                            ),
                            schema = @Schema(implementation = TodoList.class)
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
                                            summary = "Example TodoList",
                                            description = """
                                                    Provides an example `TodoList` returned after successfully
                                                    inserting into the database:
                                                    """,
                                            value = """
                                                    {
                                                      "id": "task-id",
                                                      "name": "Spring cleaning tasks"
                                                    }
                                                    """
                                    ),
                                    schema = @Schema(implementation = TodoList.class)
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
                                                    list is invalid:
                                                    """,
                                            value = """
                                                    {
                                                      "status": 400,
                                                      "message": "Todo list name cannot be empty"
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
                                                    Provides an example `ErrorResponse` returned when a `TodoList`
                                                    already exists with the specified `id`:
                                                    """,
                                            value = """
                                                    {
                                                      "status": 409,
                                                      "message": "Todo list already exists"
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
