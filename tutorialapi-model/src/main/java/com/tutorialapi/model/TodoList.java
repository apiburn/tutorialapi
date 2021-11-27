package com.tutorialapi.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.Objects;

@Schema(
        name = "TodoList",
        title = "Todo List",
        description = """
                A list containing a collection of items representing tasks that need to be completed.
                """,
        example = """
                {
                  "id": "list-id",
                  "name": "Lawn care tasks for this weekend"
                }
                """
)
public class TodoList {
    @NotEmpty(message = "Todo list id cannot be empty")
    @Size(min = 1, max = 36, message = "Todo list id must have at least 1 and no more than 36 characters")
    private String id;

    @NotEmpty(message = "Todo list name cannot be empty")
    @Size(min = 1, max = 200, message = "Todo list name must have at least 1 and no more than 200 characters")
    private String name;

    @Schema(
            name = "id",
            title = "ID",
            description = """
                The identifier used to uniquely represent this `TodoList`.
                """,
            example = """
                my-list-id
                123e4567-e89b-12d3-a456-556642440000
                """
    )
    public String getId() {
        return id;
    }

    public TodoList setId(String id) {
        this.id = id;
        return this;
    }

    @Schema(
            name = "name",
            title = "Name",
            description = """
                The name used to describe the contents of this `TodoList`.
                """,
            example = """
                Spring cleaning
                Lawn care tasks
                """
    )
    public String getName() {
        return name;
    }

    public TodoList setName(String name) {
        this.name = name;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TodoList todoList = (TodoList) o;
        return Objects.equals(id, todoList.id) && Objects.equals(name, todoList.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }

    @Override
    public String toString() {
        return "TodoList{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
