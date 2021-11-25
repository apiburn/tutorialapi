package com.tutorialapi.db;

import com.tutorialapi.db.service.TodoItemService;
import com.tutorialapi.db.service.TodoListService;
import com.tutorialapi.db.service.sqlite.SqliteTodoItemService;
import com.tutorialapi.db.service.sqlite.SqliteTodoListService;

import javax.sql.DataSource;

public class DefaultServiceFactory implements ServiceFactory {
    private final TodoListService todoListService;
    private final TodoItemService todoItemService;

    public DefaultServiceFactory(DataSource dataSource) {
        todoListService = new SqliteTodoListService(dataSource);
        todoItemService = new SqliteTodoItemService(dataSource);
    }

    @Override
    public TodoListService getTodoListService() {
        return todoListService;
    }

    @Override
    public TodoItemService getTodoItemService() {
        return todoItemService;
    }
}