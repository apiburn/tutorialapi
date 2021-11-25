package com.tutorialapi.rest.exception;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ErrorResponse {
    private final int status;
    private final String message;

    @JsonCreator
    public ErrorResponse(@JsonProperty("status") int status,
                         @JsonProperty("message") String message) {
        this.status = status;
        this.message = message;
    }

    public int getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }
}
