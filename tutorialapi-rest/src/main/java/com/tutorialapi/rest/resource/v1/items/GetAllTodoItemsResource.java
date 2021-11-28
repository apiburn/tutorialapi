package com.tutorialapi.rest.resource.v1.items;

import com.tutorialapi.model.TodoItem;
import com.tutorialapi.model.user.RapidApiPrincipal;
import com.tutorialapi.rest.Environment;
import com.tutorialapi.rest.exception.ErrorResponse;
import com.tutorialapi.rest.resource.v1.BaseResource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.SecurityContext;

import java.util.List;
import java.util.function.Supplier;

@Path("/v1/lists/{listId}/items")
public class GetAllTodoItemsResource extends BaseResource {
    private final Supplier<Environment> environmentSupplier;

    @Inject
    public GetAllTodoItemsResource(Supplier<Environment> environmentSupplier) {
        this.environmentSupplier = environmentSupplier;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            tags = "Todo Items",
            summary = "Get All Items",
            description = """
                    Fetch all `TodoItem`s from the specified `TodoList`.

                    This example shows how to use `curl` to fetch the `TodoItem`s:

                    ```bash
                    curl -sk https://localhost:8443/api/lists/list-id/items -XGET \\
                         -H "X-RapidAPI-Proxy-Secret: secret" \\
                         -H "X-RapidAPI-User: user" \\
                         -H "X-RapidAPI-Subscription: BASIC"
                    ```

                    After successfully fetching the available `TodoItem`s, this endpoint will return them in a list:

                    ```json
                    [
                      {
                        "id": "item1",
                        "task": "Mow the lawn",
                        "done": false
                      },
                      {
                        "id": "item2",
                        "task": "Clean the gutters",
                        "done": false
                      }
                    ]
                    ```

                    This endpoint will return an empty list for `TodoList`s that have no `TodoItem`s in them:

                    ```json
                    [ ]
                    ```

                    If a list with the specified `listId` does not exist, this endpoint will return a `404 Not Found`
                    response like this example:

                    ```json
                    {
                      "status": 404,
                      "message": "List with id list-id not found"
                    }
                    ```
                    """,
            operationId = "get-all-items",
            responses = {
                    @ApiResponse(
                            description = "Successful Fetch",
                            responseCode = "200",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON,
                                    examples = @ExampleObject(
                                            name = "success",
                                            summary = "Example TodoItems",
                                            description = """
                                                    Provides an example list of `TodoItem`s returned after successfully
                                                    fetching items for a list:
                                                    """,
                                            value = """
                                                    [
                                                      {
                                                        "id": "item1",
                                                        "task": "Mow the lawn",
                                                        "done": false
                                                      },
                                                      {
                                                        "id": "item2",
                                                        "task": "Clean the gutters",
                                                        "done": false
                                                      }
                                                    ]
                                                    """
                                    ),
                                    array = @ArraySchema(schema = @Schema(implementation = TodoItem.class))
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
                                                      "message": "The requested TodoList was not found"
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
    public List<TodoItem> getAllTodoItems(@Context SecurityContext securityContext,
                                          @Parameter(
                                                  name = "listId",
                                                  in = ParameterIn.PATH,
                                                  description = """
                                                          The unique id of the list containing the items to fetch
                                                          """,
                                                  required = true,
                                                  example = "my-list-id"
                                          )
                                          @PathParam("listId") String listId) {
        RapidApiPrincipal principal = (RapidApiPrincipal) securityContext.getUserPrincipal();
        return environmentSupplier.get().getServiceFactory().getTodoItemService().getAll(principal, listId);
    }
}
