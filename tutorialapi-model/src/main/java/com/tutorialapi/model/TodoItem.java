package com.tutorialapi.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.Objects;

@Schema(
        name = "TodoItem",
        title = "Todo Item",
        description = """
                Represents a specific task that needs to be completed.
                """,
        example = """
                {
                  "id": "item-id",
                  "task": "Mow the lawn",
                  "done": false
                }
                """
)
public class TodoItem {
    @NotEmpty(message = "Todo item id cannot be empty")
    @Size(min = 1, max = 36, message = "Todo item id must have at least 1 and no more than 36 characters")
    private String id;

    @NotEmpty(message = "Todo item task cannot be empty")
    @Size(min = 1, max = 200, message = "Todo item task must have at least 1 and no more than 200 characters")
    private String task;

    private boolean done = false;

    @Schema(
            name = "id",
            title = "ID",
            description = """
                The identifier used to uniquely represent this `TodoItem`.
                """,
            example = """
                my-item-id
                123e4567-e89b-12d3-a456-556642440000
                """
    )
    public String getId() {
        return id;
    }

    public TodoItem setId(String id) {
        this.id = id;
        return this;
    }

    @Schema(
            name = "task",
            title = "Task",
            description = """
                A description of the task that needs to be completed.
                """,
            example = """
                Mow the lawn
                Clean the gutters
                """
    )
    public String getTask() {
        return task;
    }

    public TodoItem setTask(String task) {
        this.task = task;
        return this;
    }

    @Schema(
            name = "done",
            title = "Done",
            description = """
                A flag indicating whether this `TodoItem` task has been completed or not.
                """,
            example = """
                true
                """
    )
    public boolean isDone() {
        return done;
    }

    public TodoItem setDone(boolean done) {
        this.done = done;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TodoItem todoItem = (TodoItem) o;
        return done == todoItem.done && Objects.equals(id, todoItem.id) && Objects.equals(task, todoItem.task);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, task, done);
    }

    @Override
    public String toString() {
        return "TodoItem{" +
                "id='" + id + '\'' +
                ", task='" + task + '\'' +
                ", done=" + done +
                '}';
    }
}
