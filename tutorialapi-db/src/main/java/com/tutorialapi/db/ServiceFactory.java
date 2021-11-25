package com.tutorialapi.db;

import com.tutorialapi.db.service.TodoItemService;
import com.tutorialapi.db.service.TodoListService;

public interface ServiceFactory {
    TodoListService getTodoListService();
    TodoItemService getTodoItemService();
}
