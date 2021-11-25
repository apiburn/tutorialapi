package com.tutorialapi.model;

import java.util.Objects;

public class TodoList {
    private String id;
    private String name;

    public String getId() {
        return id;
    }

    public TodoList setId(String id) {
        this.id = id;
        return this;
    }

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
