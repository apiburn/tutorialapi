package com.tutorialapi.rest.resource.v1;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.ws.rs.BadRequestException;

import java.util.Set;
import java.util.stream.Collectors;

public abstract class BaseResource {
    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    protected <T> void validate(T object) {
        Set<ConstraintViolation<T>> violations = validator.validate(object);
        if (!violations.isEmpty()) {
            String errorMessage = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .sorted()
                    .collect(Collectors.joining("; "));
            throw new BadRequestException(errorMessage);
        }
    }
}
