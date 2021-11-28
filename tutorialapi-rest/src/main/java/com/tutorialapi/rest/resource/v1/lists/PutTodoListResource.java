package com.tutorialapi.rest.resource.v1.lists;

import com.tutorialapi.model.TodoList;
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
import java.util.function.Supplier;

@Path("/v1/lists/{listId}")
public class PutTodoListResource extends BaseResource {
    private final Supplier<Environment> environmentSupplier;

    @Inject
    public PutTodoListResource(Supplier<Environment> environmentSupplier) {
        this.environmentSupplier = environmentSupplier;
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            tags = "Todo Lists",
            summary = "Update List",
            description = """
                    Update an existing `TodoList`.

                    This example shows how to use `curl` to update an existing `TodoList`:

                    ```bash
                    curl -sk https://localhost:8443/api/lists/list-id -XPUT \\
                         -H "X-RapidAPI-Proxy-Secret: secret" \\
                         -H "X-RapidAPI-User: user" \\
                         -H "X-RapidAPI-Subscription: BASIC" \\
                         -H "Content-Type: application/json" \\
                         -d '{"name": "Spring cleaning tasks"}'
                    ```

                    After successfully updating a `TodoList`, this endpoint will return the modified list:

                    ```json
                    {
                      "id": "list-id",
                      "name": "Spring cleaning tasks"
                    }
                    ```
                    """,
            operationId = "update-list",
            requestBody = @RequestBody(
                    description = """
                            The request body is expected to provide the updated `TodoList`.
                            """,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            examples = @ExampleObject(
                                    name = "todo-list-body",
                                    summary = "Example TodoList",
                                    description = """
                                            Provides an example `TodoList` request body to update:
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
                            description = "Successful Update",
                            responseCode = "200",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON,
                                    examples = @ExampleObject(
                                            name = "success",
                                            summary = "Example TodoList",
                                            description = """
                                                    Provides an example `TodoList` returned after successfully
                                                    updating the existing list in the database:
                                                    """,
                                            value = """
                                                    {
                                                      "id": "list-id",
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
    public TodoList putTodoList(@Context SecurityContext securityContext,
                                @Parameter(
                                        name = "listId",
                                        in = ParameterIn.PATH,
                                        description = "The unique id of the list to update",
                                        required = true,
                                        example = "my-list-id"
                                )
                                @PathParam("listId") String listId,
                                TodoList todoList) {
        todoList.setId(listId);
        todoList.setName(Optional.ofNullable(todoList.getName()).map(StringEscapeUtils::escapeHtml4).orElse(null));
        validate(todoList);

        RapidApiPrincipal principal = (RapidApiPrincipal) securityContext.getUserPrincipal();
        if (environmentSupplier.get().getServiceFactory().getTodoListService().update(principal, todoList)) {
            return todoList;
        }
        throw new NotFoundException("List with id " + listId + " not found");
    }
}
