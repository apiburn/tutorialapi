package com.tutorialapi.model;

import java.util.Objects;

public class TodoItem {
    private String id;
    private String task;
    private boolean done = false;

    public String getId() {
        return id;
    }

    public TodoItem setId(String id) {
        this.id = id;
        return this;
    }

    public String getTask() {
        return task;
    }

    public TodoItem setTask(String task) {
        this.task = task;
        return this;
    }

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
