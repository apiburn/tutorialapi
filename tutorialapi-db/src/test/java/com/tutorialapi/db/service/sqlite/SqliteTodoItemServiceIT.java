package com.tutorialapi.db.service.sqlite;

import com.tutorialapi.db.DataSourceExtension;
import com.tutorialapi.db.exception.ConflictException;
import com.tutorialapi.model.TodoItem;
import com.tutorialapi.model.TodoList;
import com.tutorialapi.model.user.RapidApiPrincipal;
import com.tutorialapi.model.user.Subscription;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.sql.DataSource;
import java.util.List;
import java.util.Optional;

@ExtendWith(DataSourceExtension.class)
public class SqliteTodoItemServiceIT {
    private final SqliteTodoListService todoListService;
    private final SqliteTodoItemService todoItemService;

    private final RapidApiPrincipal principal1 = new RapidApiPrincipal("proxy-secret", "user1", Subscription.BASIC);
    private final RapidApiPrincipal principal2 = new RapidApiPrincipal("proxy-secret", "user2", Subscription.BASIC);

    private final TodoList list1 = new TodoList().setId("id1").setName("name1");
    private final TodoList list2 = new TodoList().setId("id2").setName("name2");

    public SqliteTodoItemServiceIT(DataSource dataSource) {
        this.todoListService = new SqliteTodoListService(dataSource);
        this.todoItemService = new SqliteTodoItemService(dataSource);
    }

    @BeforeEach
    public void beforeEach() {
        todoListService.create(principal1, list1);
        todoListService.create(principal1, list2);
    }

    @Test
    public void testGetWithNoItems() {
        Optional<TodoItem> fetched = todoItemService.get(principal1, list1.getId(), "id");
        Assertions.assertTrue(fetched.isEmpty());
    }

    @Test
    public void testGetWithWrongPrincipal() {
        TodoItem item = new TodoItem().setId("id").setTask("task").setDone(false);
        Assertions.assertTrue(todoItemService.create(principal1, list1.getId(), item));

        Optional<TodoItem> fetched = todoItemService.get(principal2, list1.getId(), item.getId());
        Assertions.assertTrue(fetched.isEmpty());
    }

    @Test
    public void testGetWithWrongId() {
        TodoItem item = new TodoItem().setId("id").setTask("task").setDone(false);
        Assertions.assertTrue(todoItemService.create(principal1, list1.getId(), item));

        Optional<TodoItem> fetched = todoItemService.get(principal1, list1.getId(), "wrong");
        Assertions.assertTrue(fetched.isEmpty());
    }

    @Test
    public void testGetWithOneItem() {
        TodoItem item = new TodoItem().setId("id").setTask("task").setDone(false);
        Assertions.assertTrue(todoItemService.create(principal1, list1.getId(), item));

        Optional<TodoItem> fetched = todoItemService.get(principal1, list1.getId(), item.getId());
        Assertions.assertTrue(fetched.isPresent());
        Assertions.assertEquals(item, fetched.get());
    }

    @Test
    public void testGetWithMultipleItems() {
        TodoItem item1 = new TodoItem().setId("id1").setTask("task1").setDone(true);
        TodoItem item2 = new TodoItem().setId("id2").setTask("task2").setDone(false);
        Assertions.assertTrue(todoItemService.create(principal1, list1.getId(), item1));
        Assertions.assertTrue(todoItemService.create(principal1, list2.getId(), item2));

        Optional<TodoItem> fetched = todoItemService.get(principal1, list1.getId(), item1.getId());
        Assertions.assertTrue(fetched.isPresent());
        Assertions.assertEquals(item1, fetched.get());
    }

    @Test
    public void testGetAllWithNoItems() {
        List<TodoItem> fetched = todoItemService.getAll(principal1, list1.getId());
        Assertions.assertTrue(fetched.isEmpty());
    }

    @Test
    public void testGetAllWithWrongPrincipal() {
        TodoItem item1 = new TodoItem().setId("id1").setTask("task1").setDone(true);
        TodoItem item2 = new TodoItem().setId("id2").setTask("task2").setDone(false);
        Assertions.assertTrue(todoItemService.create(principal1, list1.getId(), item1));
        Assertions.assertTrue(todoItemService.create(principal1, list1.getId(), item2));

        List<TodoItem> fetched = todoItemService.getAll(principal2, list1.getId());
        Assertions.assertTrue(fetched.isEmpty());
    }

    @Test
    public void testGetAllWithOneItem() {
        TodoItem item = new TodoItem().setId("id").setTask("task").setDone(false);
        Assertions.assertTrue(todoItemService.create(principal1, list1.getId(), item));

        List<TodoItem> fetched = todoItemService.getAll(principal1, list1.getId());
        Assertions.assertEquals(1, fetched.size());
        Assertions.assertEquals(item, fetched.get(0));
    }

    @Test
    public void testGetAllWithMultipleItems() {
        TodoItem item1 = new TodoItem().setId("id1").setTask("task1").setDone(true);
        TodoItem item2 = new TodoItem().setId("id2").setTask("task2").setDone(false);
        Assertions.assertTrue(todoItemService.create(principal1, list1.getId(), item1));
        Assertions.assertTrue(todoItemService.create(principal1, list1.getId(), item2));

        List<TodoItem> fetched = todoItemService.getAll(principal1, list1.getId());
        Assertions.assertEquals(2, fetched.size());
        Assertions.assertEquals(item2, fetched.get(0)); // done==false items come first
        Assertions.assertEquals(item1, fetched.get(1));
    }

    @Test
    public void testCreateConflict() {
        TodoItem item = new TodoItem().setId("id").setTask("task").setDone(false);
        Assertions.assertTrue(todoItemService.create(principal1, list1.getId(), item));
        ConflictException exception = Assertions.assertThrows(ConflictException.class,
                () -> todoItemService.create(principal1, list1.getId(), item));

        Assertions.assertEquals("Todo item already exists", exception.getMessage());
    }

    @Test
    public void testCreateConflictDifferentTask() {
        TodoItem item1 = new TodoItem().setId("id").setTask("task1").setDone(false);
        TodoItem item2 = new TodoItem().setId("id").setTask("task2").setDone(false);
        Assertions.assertTrue(todoItemService.create(principal1, list1.getId(), item1));
        ConflictException exception = Assertions.assertThrows(ConflictException.class,
                () -> todoItemService.create(principal1, list1.getId(), item2));

        Assertions.assertEquals("Todo item already exists", exception.getMessage());
    }

    @Test
    public void testCreateConflictDifferentUser() {
        TodoItem item = new TodoItem().setId("id").setTask("task").setDone(false);
        Assertions.assertTrue(todoItemService.create(principal1, list1.getId(), item));
        Assertions.assertTrue(todoItemService.create(principal2, list1.getId(), item));

        Optional<TodoItem> fetched1 = todoItemService.get(principal1, list1.getId(), item.getId());
        Optional<TodoItem> fetched2 = todoItemService.get(principal2, list1.getId(), item.getId());
        Assertions.assertTrue(fetched1.isPresent());
        Assertions.assertTrue(fetched2.isPresent());
    }

    @Test
    public void testUpdateMissing() {
        TodoItem item = new TodoItem().setId("id").setTask("task").setDone(false);
        Assertions.assertFalse(todoItemService.update(principal1, list1.getId(), item));
    }

    @Test
    public void testUpdateWrongUser() {
        TodoItem item = new TodoItem().setId("id").setTask("task").setDone(false);
        Assertions.assertTrue(todoItemService.create(principal1, list1.getId(), item));

        item.setTask("updated").setDone(true);
        Assertions.assertFalse(todoItemService.update(principal2, list1.getId(), item));

        Optional<TodoItem> fetched = todoItemService.get(principal1, list1.getId(), item.getId());
        Assertions.assertTrue(fetched.isPresent());
        Assertions.assertEquals("task", fetched.get().getTask());
        Assertions.assertFalse(fetched.get().isDone());
    }

    @Test
    public void testUpdateExistsButSame() {
        TodoItem item = new TodoItem().setId("id").setTask("task").setDone(false);
        Assertions.assertTrue(todoItemService.create(principal1, list1.getId(), item));
        Assertions.assertFalse(todoItemService.update(principal1, list1.getId(), item));
    }

    @Test
    public void testUpdateDifferentTask() {
        TodoItem item = new TodoItem().setId("id").setTask("task").setDone(false);
        Assertions.assertTrue(todoItemService.create(principal1, list1.getId(), item));
        item.setTask("updated");
        Assertions.assertTrue(todoItemService.update(principal1, list1.getId(), item));

        Optional<TodoItem> fetched = todoItemService.get(principal1, list1.getId(), item.getId());
        Assertions.assertTrue(fetched.isPresent());
        Assertions.assertEquals(item, fetched.get());
    }

    @Test
    public void testUpdateDifferentDone() {
        TodoItem item = new TodoItem().setId("id").setTask("task").setDone(false);
        Assertions.assertTrue(todoItemService.create(principal1, list1.getId(), item));
        item.setDone(true);
        Assertions.assertTrue(todoItemService.update(principal1, list1.getId(), item));

        Optional<TodoItem> fetched = todoItemService.get(principal1, list1.getId(), item.getId());
        Assertions.assertTrue(fetched.isPresent());
        Assertions.assertEquals(item, fetched.get());
    }

    @Test
    public void testDeleteMissing() {
        Assertions.assertFalse(todoItemService.delete(principal1, list1.getId(), "missing"));
    }

    @Test
    public void testDeleteWrongUser() {
        TodoItem item = new TodoItem().setId("id").setTask("task").setDone(false);
        Assertions.assertTrue(todoItemService.create(principal1, list1.getId(), item));
        Assertions.assertFalse(todoItemService.delete(principal2, list1.getId(), item.getId()));

        Optional<TodoItem> fetched = todoItemService.get(principal1, list1.getId(), item.getId());
        Assertions.assertTrue(fetched.isPresent());
    }

    @Test
    public void testDeleteSuccess() {
        TodoItem item = new TodoItem().setId("id").setTask("task").setDone(false);
        Assertions.assertTrue(todoItemService.create(principal1, list1.getId(), item));
        Assertions.assertTrue(todoItemService.delete(principal1, list1.getId(), item.getId()));

        Optional<TodoItem> fetched = todoItemService.get(principal1, list1.getId(), item.getId());
        Assertions.assertTrue(fetched.isEmpty());
    }

    @Test
    public void testTruncateNone() {
        int deleted = todoItemService.truncate();
        Assertions.assertEquals(0, deleted);
    }

    @Test
    public void testTruncateWithSome() {
        TodoItem item1 = new TodoItem().setId("id1").setTask("task1").setDone(true);
        TodoItem item2 = new TodoItem().setId("id2").setTask("task2").setDone(false);
        Assertions.assertTrue(todoItemService.create(principal1, list1.getId(), item1));
        Assertions.assertTrue(todoItemService.create(principal1, list1.getId(), item2));
        Assertions.assertTrue(todoItemService.create(principal2, list2.getId(), item1));
        Assertions.assertTrue(todoItemService.create(principal2, list2.getId(), item2));

        int deleted = todoItemService.truncate();
        Assertions.assertEquals(4, deleted);
    }
}
