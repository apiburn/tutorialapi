package com.tutorialapi.rest.exception;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Objects;


@Schema(
        name = "ErrorResponse",
        title = "Error Response",
        description = """
                Provides information about any errors that occur in this API.
                """,
        example = """
                {
                  "status": 404,
                  "message": "A list with id list-id was not found"
                }
                """
)
public class ErrorResponse {
    private final int status;
    private final String message;

    @JsonCreator
    public ErrorResponse(@JsonProperty("status") int status,
                         @JsonProperty("message") String message) {
        this.status = status;
        this.message = message;
    }

    @Schema(
            name = "status",
            title = "Status",
            description = """
                Specifies the HTTP status code returned after the error condition.
                """,
            example = """
                400
                404
                500
                """
    )
    public int getStatus() {
        return status;
    }

    @Schema(
            name = "message",
            title = "Message",
            description = """
                Provides a brief message describing the error that occurred.
                """,
            example = """
                A list with id list-id was not found
                """
    )
    public String getMessage() {
        return message;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ErrorResponse that = (ErrorResponse) o;
        return status == that.status && Objects.equals(message, that.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(status, message);
    }

    @Override
    public String toString() {
        return "ErrorResponse{" +
                "status=" + status +
                ", message='" + message + '\'' +
                '}';
    }
}
