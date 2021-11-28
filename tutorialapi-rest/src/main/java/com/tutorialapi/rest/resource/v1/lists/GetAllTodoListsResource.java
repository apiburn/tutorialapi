package com.tutorialapi.rest.resource.v1.lists;

import com.tutorialapi.model.TodoList;
import com.tutorialapi.model.user.RapidApiPrincipal;
import com.tutorialapi.rest.Environment;
import com.tutorialapi.rest.exception.ErrorResponse;
import com.tutorialapi.rest.resource.v1.BaseResource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.SecurityContext;

import java.util.List;
import java.util.function.Supplier;

@Path("/v1/lists")
public class GetAllTodoListsResource extends BaseResource {
    private final Supplier<Environment> environmentSupplier;

    @Inject
    public GetAllTodoListsResource(Supplier<Environment> environmentSupplier) {
        this.environmentSupplier = environmentSupplier;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            tags = "Todo Lists",
            summary = "Get All Lists",
            description = """
                    Fetch all `TodoList`s.

                    This example shows how to use `curl` to fetch all `TodoLists`s:

                    ```bash
                    curl -sk https://localhost:8443/api/lists -XGET \\
                         -H "X-RapidAPI-Proxy-Secret: secret" \\
                         -H "X-RapidAPI-User: user" \\
                         -H "X-RapidAPI-Subscription: BASIC"
                    ```

                    After successfully fetching the available `TodoList`s, this endpoint will return them in a list:

                    ```json
                    [
                      {
                        "id": "list1",
                        "name": "Spring cleaning tasks"
                      },
                      {
                        "id": "list2",
                        "name": "Lawn care chores"
                      }
                    ]
                    ```

                    This endpoint will return an empty list if there are no `TodoList`s available:

                    ```json
                    [ ]
                    ```
                    """,
            operationId = "get-all-lists",
            responses = {
                    @ApiResponse(
                            description = "Successful Fetch",
                            responseCode = "200",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON,
                                    examples = @ExampleObject(
                                            name = "success",
                                            summary = "Example TodoLists",
                                            description = """
                                                    Provides an example list of `TodoLists`s returned after successfully
                                                    fetching available lists:
                                                    """,
                                            value = """
                                                    [
                                                      {
                                                        "id": "list1",
                                                        "name": "Spring cleaning tasks"
                                                      },
                                                      {
                                                        "id": "list2",
                                                        "name": "Lawn care chores"
                                                      }
                                                    ]
                                                    """
                                    ),
                                    array = @ArraySchema(schema = @Schema(implementation = TodoList.class))
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
    public List<TodoList> getAllTodoLists(@Context SecurityContext securityContext) {
        RapidApiPrincipal principal = (RapidApiPrincipal) securityContext.getUserPrincipal();
        return environmentSupplier.get().getServiceFactory().getTodoListService().getAll(principal);
    }
}
