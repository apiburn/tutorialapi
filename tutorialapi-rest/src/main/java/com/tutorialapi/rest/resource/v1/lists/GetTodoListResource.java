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
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.SecurityContext;

import java.util.function.Supplier;

@Path("/v1/lists/{listId}")
public class GetTodoListResource extends BaseResource {
    private final Supplier<Environment> environmentSupplier;

    @Inject
    public GetTodoListResource(Supplier<Environment> environmentSupplier) {
        this.environmentSupplier = environmentSupplier;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            tags = "Todo Lists",
            summary = "Get List",
            description = """
                    Fetch a specific `TodoList`.

                    This example shows how to use `curl` to fetch a `TodoList`:

                    ```bash
                    curl -sk https://localhost:8443/api/lists/list-id -XGET \\
                         -H "X-RapidAPI-Proxy-Secret: secret" \\
                         -H "X-RapidAPI-User: user" \\
                         -H "X-RapidAPI-Subscription: BASIC"
                    ```

                    After successfully fetching a `TodoList`, this endpoint will return the list:

                    ```json
                    {
                      "id": "list-id",
                      "name": "Spring cleanup tasks"
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
            operationId = "get-list",
            responses = {
                    @ApiResponse(
                            description = "Successful Fetch",
                            responseCode = "200",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON,
                                    examples = @ExampleObject(
                                            name = "success",
                                            summary = "Example TodoList",
                                            description = """
                                                    Provides an example `TodoList` returned after successfully
                                                    fetching the list:
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
    public TodoList getTodoList(@Context SecurityContext securityContext,
                                @Parameter(
                                        name = "listId",
                                        in = ParameterIn.PATH,
                                        description = "The unique id of the list to fetch",
                                        required = true,
                                        example = "my-list-id"
                                )
                                @PathParam("listId") String listId) {
        RapidApiPrincipal principal = (RapidApiPrincipal) securityContext.getUserPrincipal();
        return environmentSupplier.get().getServiceFactory().getTodoListService().get(principal, listId)
                .orElseThrow(() -> new NotFoundException("List with id " + listId + " not found"));
    }
}
