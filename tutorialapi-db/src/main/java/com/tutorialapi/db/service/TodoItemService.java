package com.tutorialapi.db.service;

import com.tutorialapi.model.TodoItem;
import com.tutorialapi.model.user.RapidApiPrincipal;

import java.util.List;
import java.util.Optional;

public interface TodoItemService {
    Optional<TodoItem> get(RapidApiPrincipal principal, String listId, String id);
    List<TodoItem> getAll(RapidApiPrincipal principal, String listId);
    boolean create(RapidApiPrincipal principal, String listId, TodoItem todoItem);
    boolean update(RapidApiPrincipal principal, String listId, TodoItem todoItem);
    Optional<TodoItem> delete(RapidApiPrincipal principal, String listId, String id);
    int truncate();
}
