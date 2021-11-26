package com.tutorialapi.db.service.sqlite;

import com.tutorialapi.db.DataSourceExtension;
import com.tutorialapi.db.exception.ConflictException;
import com.tutorialapi.model.TodoList;
import com.tutorialapi.model.user.RapidApiPrincipal;
import com.tutorialapi.model.user.Subscription;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.sql.DataSource;
import java.util.List;
import java.util.Optional;

@ExtendWith(DataSourceExtension.class)
public class SqliteTodoListServiceIT {
    private final SqliteTodoListService todoListService;

    private final RapidApiPrincipal principal1 = new RapidApiPrincipal("proxy-secret", "user1", Subscription.BASIC);
    private final RapidApiPrincipal principal2 = new RapidApiPrincipal("proxy-secret", "user2", Subscription.BASIC);

    public SqliteTodoListServiceIT(DataSource dataSource) {
        this.todoListService = new SqliteTodoListService(dataSource);
    }

    @Test
    public void testGetWithNoLists() {
        Optional<TodoList> fetched = todoListService.get(principal1, "id");
        Assertions.assertTrue(fetched.isEmpty());
    }

    @Test
    public void testGetWithWrongPrincipal() {
        TodoList list = new TodoList().setId("id").setName("name");
        Assertions.assertTrue(todoListService.create(principal1, list));

        Optional<TodoList> fetched = todoListService.get(principal2, list.getId());
        Assertions.assertTrue(fetched.isEmpty());
    }

    @Test
    public void testGetWithWrongId() {
        TodoList wrongId = new TodoList().setId("id").setName("name");
        Assertions.assertTrue(todoListService.create(principal1, wrongId));

        Optional<TodoList> fetched = todoListService.get(principal1, "wrong");
        Assertions.assertTrue(fetched.isEmpty());
    }

    @Test
    public void testGetWithOneList() {
        TodoList list = new TodoList().setId("id").setName("name");
        Assertions.assertTrue(todoListService.create(principal1, list));

        Optional<TodoList> fetched = todoListService.get(principal1, list.getId());
        Assertions.assertTrue(fetched.isPresent());
        Assertions.assertEquals(list, fetched.get());
    }

    @Test
    public void testGetWithMultipleLists() {
        TodoList list1 = new TodoList().setId("id1").setName("name");
        TodoList list2 = new TodoList().setId("id2").setName("name");
        Assertions.assertTrue(todoListService.create(principal1, list1));
        Assertions.assertTrue(todoListService.create(principal1, list2));

        Optional<TodoList> fetched = todoListService.get(principal1, list1.getId());
        Assertions.assertTrue(fetched.isPresent());
        Assertions.assertEquals(list1, fetched.get());
    }

    @Test
    public void testGetAllWithNoLists() {
        List<TodoList> fetched = todoListService.getAll(principal1);
        Assertions.assertTrue(fetched.isEmpty());
    }

    @Test
    public void testGetAllWithWrongPrincipal() {
        TodoList list = new TodoList().setId("id").setName("name");
        Assertions.assertTrue(todoListService.create(principal1, list));

        List<TodoList> fetched = todoListService.getAll(principal2);
        Assertions.assertTrue(fetched.isEmpty());
    }

    @Test
    public void testGetAllWithOneList() {
        TodoList list = new TodoList().setId("id").setName("name");
        Assertions.assertTrue(todoListService.create(principal1, list));

        List<TodoList> fetched = todoListService.getAll(principal1);
        Assertions.assertEquals(1, fetched.size());
        Assertions.assertEquals(list, fetched.get(0));
    }

    @Test
    public void testGetAllWithMultipleLists() {
        TodoList list1 = new TodoList().setId("id1").setName("name1");
        TodoList list2 = new TodoList().setId("id2").setName("name2");
        Assertions.assertTrue(todoListService.create(principal1, list1));
        Assertions.assertTrue(todoListService.create(principal1, list2));

        List<TodoList> fetched = todoListService.getAll(principal1);
        Assertions.assertEquals(2, fetched.size());
        Assertions.assertEquals(list1, fetched.get(0));
        Assertions.assertEquals(list2, fetched.get(1));
    }

    @Test
    public void testCreateConflict() {
        TodoList list = new TodoList().setId("id").setName("name");
        Assertions.assertTrue(todoListService.create(principal1, list));
        ConflictException exception = Assertions.assertThrows(ConflictException.class,
                () -> todoListService.create(principal1, list));

        Assertions.assertEquals("Todo list already exists", exception.getMessage());
    }

    @Test
    public void testCreateConflictDifferentName() {
        TodoList list1 = new TodoList().setId("id").setName("name1");
        TodoList list2 = new TodoList().setId("id").setName("name2");
        Assertions.assertTrue(todoListService.create(principal1, list1));
        ConflictException exception = Assertions.assertThrows(ConflictException.class,
                () -> todoListService.create(principal1, list2));

        Assertions.assertEquals("Todo list already exists", exception.getMessage());
    }

    @Test
    public void testCreateConflictDifferentUser() {
        TodoList list1 = new TodoList().setId("id").setName("name");
        TodoList list2 = new TodoList().setId("id").setName("name");
        Assertions.assertTrue(todoListService.create(principal1, list1));
        Assertions.assertTrue(todoListService.create(principal2, list2));

        Optional<TodoList> fetched1 = todoListService.get(principal1, list1.getId());
        Optional<TodoList> fetched2 = todoListService.get(principal2, list2.getId());
        Assertions.assertTrue(fetched1.isPresent());
        Assertions.assertTrue(fetched2.isPresent());
    }

    @Test
    public void testUpdateMissing() {
        TodoList list = new TodoList().setId("id").setName("name");
        Assertions.assertFalse(todoListService.update(principal1, list));
    }

    @Test
    public void testUpdateWrongUser() {
        TodoList list = new TodoList().setId("id").setName("name");
        Assertions.assertTrue(todoListService.create(principal1, list));

        list.setName("updated");
        Assertions.assertFalse(todoListService.update(principal2, list));

        Optional<TodoList> fetched = todoListService.get(principal1, list.getId());
        Assertions.assertTrue(fetched.isPresent());
        Assertions.assertEquals("name", fetched.get().getName());
    }

    @Test
    public void testUpdateExistsButSame() {
        TodoList list = new TodoList().setId("id").setName("name");
        Assertions.assertTrue(todoListService.create(principal1, list));
        Assertions.assertFalse(todoListService.update(principal1, list));
    }

    @Test
    public void testUpdateDifferentName() {
        TodoList list = new TodoList().setId("id").setName("name");
        Assertions.assertTrue(todoListService.create(principal1, list));
        list.setName("updated");
        Assertions.assertTrue(todoListService.update(principal1, list));

        Optional<TodoList> fetched = todoListService.get(principal1, list.getId());
        Assertions.assertTrue(fetched.isPresent());
        Assertions.assertEquals(list, fetched.get());
    }

    @Test
    public void testDeleteMissing() {
        Optional<TodoList> deleted = todoListService.delete(principal1, "missing");
        Assertions.assertFalse(deleted.isPresent());
    }

    @Test
    public void testDeleteWrongUser() {
        TodoList list = new TodoList().setId("id").setName("name");
        Assertions.assertTrue(todoListService.create(principal1, list));
        Optional<TodoList> deleted = todoListService.delete(principal2, list.getId());
        Assertions.assertFalse(deleted.isPresent());

        Optional<TodoList> fetched = todoListService.get(principal1, list.getId());
        Assertions.assertTrue(fetched.isPresent());
    }

    @Test
    public void testDeleteSuccess() {
        TodoList list = new TodoList().setId("id").setName("name");
        Assertions.assertTrue(todoListService.create(principal1, list));
        Optional<TodoList> deleted = todoListService.delete(principal1, list.getId());
        Assertions.assertTrue(deleted.isPresent());

        Optional<TodoList> fetched = todoListService.get(principal1, list.getId());
        Assertions.assertTrue(fetched.isEmpty());
    }

    @Test
    public void testTruncateNone() {
        int deleted = todoListService.truncate();
        Assertions.assertEquals(0, deleted);
    }

    @Test
    public void testTruncateWithSome() {
        TodoList list1 = new TodoList().setId("id1").setName("name");
        TodoList list2 = new TodoList().setId("id2").setName("name");
        Assertions.assertTrue(todoListService.create(principal1, list1));
        Assertions.assertTrue(todoListService.create(principal1, list2));

        int deleted = todoListService.truncate();
        Assertions.assertEquals(2, deleted);
    }
}
