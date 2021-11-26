package com.tutorialapi.db.service;

import com.tutorialapi.model.TodoList;
import com.tutorialapi.model.user.RapidApiPrincipal;

import java.util.List;
import java.util.Optional;

public interface TodoListService {
    Optional<TodoList> get(RapidApiPrincipal principal, String id);
    List<TodoList> getAll(RapidApiPrincipal principal);
    boolean create(RapidApiPrincipal principal, TodoList todoList);
    boolean update(RapidApiPrincipal principal, TodoList todoList);
    Optional<TodoList> delete(RapidApiPrincipal principal, String id);
    int truncate();
}
