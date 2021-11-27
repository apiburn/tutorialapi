package com.tutorialapi.rest.resource.v1.lists;

import com.tutorialapi.db.ServiceFactory;
import com.tutorialapi.model.TodoList;
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

@Path("/v1/lists/{listId}")
public class DeleteTodoListResource extends BaseResource {
    private final ServiceFactory serviceFactory;

    @Inject
    public DeleteTodoListResource(ServiceFactory serviceFactory) {
        this.serviceFactory = serviceFactory;
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            tags = "Todo Lists",
            summary = "Delete List",
            description = """
                    Delete the specified `TodoList`. This endpoint:

                    * Fetches the specified `TodoList`.
                    * Deletes the `TodoList` if found.
                    * Returns the fetched `TodoList` to the caller.

                    This example shows how to use `curl` to perform the deletion:

                    ```bash
                    curl -sk https://localhost:8443/api/lists/list-id -XDELETE \\
                         -H "X-RapidAPI-Proxy-Secret: secret" \\
                         -H "X-RapidAPI-User: user" \\
                         -H "X-RapidAPI-Subscription: BASIC"
                    ```

                    After successfully deleting a `TodoList`, this endpoint will return the `TodoList` that was
                    deleted:

                    ```json
                    {
                      "id": "list-id",
                      "name": "Spring cleaning tasks"
                    }
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
            operationId = "delete-list",
            responses = {
                    @ApiResponse(
                            description = "Successful Deletion",
                            responseCode = "200",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON,
                                    examples = @ExampleObject(
                                            name = "success",
                                            summary = "Example TodoList",
                                            description = """
                                                    Provides an example `TodoList` returned after successful deletion:
                                                    """,
                                            value = """
                                                    {
                                                      "id": "my-list-id",
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
    public TodoList deleteTodoList(@Context SecurityContext securityContext,
                                   @Parameter(
                                           name = "listId",
                                           in = ParameterIn.PATH,
                                           description = "The unique id of the list to delete",
                                           required = true,
                                           example = "my-list-id"
                                   )
                                   @PathParam("listId") String listId) {
        RapidApiPrincipal principal = (RapidApiPrincipal) securityContext.getUserPrincipal();
        return serviceFactory.getTodoListService().delete(principal, listId)
                .orElseThrow(() -> new NotFoundException("List with id " + listId + " not found"));
    }
}
