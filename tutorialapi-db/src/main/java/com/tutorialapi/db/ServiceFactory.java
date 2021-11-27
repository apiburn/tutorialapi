package com.tutorialapi.db;

import com.tutorialapi.db.service.ApiKeyService;
import com.tutorialapi.db.service.TodoItemService;
import com.tutorialapi.db.service.TodoListService;

public interface ServiceFactory {
    ApiKeyService getApiKeyService();
    TodoListService getTodoListService();
    TodoItemService getTodoItemService();
}
