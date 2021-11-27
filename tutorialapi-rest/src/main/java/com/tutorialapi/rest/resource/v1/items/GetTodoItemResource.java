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
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.SecurityContext;

@Path("/v1/lists/{listId}/items/{id}")
public class GetTodoItemResource extends BaseResource {
    private final ServiceFactory serviceFactory;

    @Inject
    public GetTodoItemResource(ServiceFactory serviceFactory) {
        this.serviceFactory = serviceFactory;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            tags = "Todo Items",
            summary = "Get Item",
            description = """
                    Fetch a specific `TodoItem` from the specified `TodoList`.

                    This example shows how to use `curl` to fetch a `TodoItem`:

                    ```bash
                    curl -sk https://localhost:8443/api/lists/list-id/items/item-id -XGET \\
                         -H "X-RapidAPI-Proxy-Secret: secret" \\
                         -H "X-RapidAPI-User: user" \\
                         -H "X-RapidAPI-Subscription: BASIC"
                    ```

                    After successfully fetching a `TodoItem`, this endpoint will return the item:

                    ```json
                    {
                      "id": "item-id",
                      "task": "Mow the lawn",
                      "done": false
                    }
                    ```

                    If a list with the specified `listId` does not exist, or if an item with the specified `id` does
                    not exist inside the list, then this endpoint will return a `404 Not Found` response like this
                    example:

                    ```json
                    {
                      "status": 404,
                      "message": "Item with id item-id not found in list with id list-id"
                    }
                    ```
                    """,
            operationId = "get-item",
            responses = {
                    @ApiResponse(
                            description = "Successful Fetch",
                            responseCode = "200",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON,
                                    examples = @ExampleObject(
                                            name = "success",
                                            summary = "Example TodoItem",
                                            description = """
                                                    Provides an example `TodoItem` returned after successfully
                                                    fetching the item from a list:
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
                            description = "Not Found",
                            responseCode = "404",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON,
                                    examples = @ExampleObject(
                                            name = "not-found",
                                            summary = "Example ErrorResponse",
                                            description = """
                                                    Provides an example `ErrorResponse` returned when the requested
                                                    list or the requested item is not found:
                                                    """,
                                            value = """
                                                    {
                                                      "status": 404,
                                                      "message": "Item with id item-id not found in list with id list-id"
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
    public TodoItem getTodoItem(@Context SecurityContext securityContext,
                                @Parameter(
                                        name = "listId",
                                        in = ParameterIn.PATH,
                                        description = "The unique id of the list containing the item to fetch",
                                        required = true,
                                        example = "my-list-id"
                                )
                                @PathParam("listId") String listId,
                                @Parameter(
                                        name = "id",
                                        in = ParameterIn.PATH,
                                        description = "The unique id of the item to fetch",
                                        required = true,
                                        example = "my-item-id"
                                )
                                @PathParam("id") String id) {
        RapidApiPrincipal principal = (RapidApiPrincipal) securityContext.getUserPrincipal();
        return serviceFactory.getTodoItemService().get(principal, listId, id).orElseThrow(() ->
                new NotFoundException("Item with id " + id + " not found in list with id " + listId));
    }
}
