package com.tutorialapi.db.service.sqlite;

import com.tutorialapi.db.exception.ConflictException;
import com.tutorialapi.db.service.TodoItemService;
import com.tutorialapi.model.TodoItem;
import com.tutorialapi.model.user.RapidApiPrincipal;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SqliteTodoItemService implements TodoItemService {
    private final DataSource dataSource;

    public SqliteTodoItemService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Optional<TodoItem> get(RapidApiPrincipal principal, String listId, String id) {
        String sql = "SELECT * FROM todo_items WHERE user_id = ? AND list_id = ? AND id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            int index = 0;
            ps.setString(++index, principal.getUser());
            ps.setString(++index, listId);
            ps.setString(++index, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(
                            new TodoItem()
                                    .setId(rs.getString("id"))
                                    .setTask(rs.getString("task"))
                                    .setDone(rs.getBoolean("done"))
                    );
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch item: " + e.getMessage(), e);
        }
    }

    @Override
    public List<TodoItem> getAll(RapidApiPrincipal principal, String listId) {
        String sql = "SELECT * FROM todo_items WHERE user_id = ? AND list_id = ? ORDER BY done, task";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            int index = 0;
            ps.setString(++index, principal.getUser());
            ps.setString(++index, listId);
            List<TodoItem> todoItems = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    todoItems.add(
                            new TodoItem()
                                    .setId(rs.getString("id"))
                                    .setTask(rs.getString("task"))
                                    .setDone(rs.getBoolean("done"))
                    );
                }
            }
            return todoItems;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch items: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean create(RapidApiPrincipal principal, String listId, TodoItem todoItem) {
        String sql = "INSERT INTO todo_items (user_id, list_id, id, task, done) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            int index = 0;
            ps.setString(++index, principal.getUser());
            ps.setString(++index, listId);
            ps.setString(++index, todoItem.getId());
            ps.setString(++index, todoItem.getTask());
            ps.setBoolean(++index, todoItem.isDone());
            if (ps.executeUpdate() > 0) {
                conn.commit();
                return true;
            }
            return false;
        } catch (SQLException e) {
            if (e.getMessage() != null && e.getMessage().contains("SQLITE_CONSTRAINT_PRIMARYKEY")) {
                throw new ConflictException("Todo item already exists");
            }
            throw new RuntimeException("Failed to create item: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean update(RapidApiPrincipal principal, String listId, TodoItem todoItem) {
        String sql = "UPDATE todo_items SET task = ?, done = ? " +
                "WHERE user_id = ? AND list_id = ? AND id = ? AND (task != ? OR done != ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            int index = 0;
            ps.setString(++index, todoItem.getTask());
            ps.setBoolean(++index, todoItem.isDone());
            ps.setString(++index, principal.getUser());
            ps.setString(++index, listId);
            ps.setString(++index, todoItem.getId());
            ps.setString(++index, todoItem.getTask());
            ps.setBoolean(++index, todoItem.isDone());
            if (ps.executeUpdate() > 0) {
                conn.commit();
                return true;
            }
            return false;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update item: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean delete(RapidApiPrincipal principal, String listId, String id) {
        String sql = "DELETE FROM todo_items WHERE user_id = ? AND list_id = ? AND id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            int index = 0;
            ps.setString(++index, principal.getUser());
            ps.setString(++index, listId);
            ps.setString(++index, id);
            if (ps.executeUpdate() > 0) {
                conn.commit();
                return true;
            }
            return false;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete item: " + e.getMessage(), e);
        }
    }

    @Override
    public int truncate() {
        String sql = "DELETE FROM todo_items";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            int deleted = ps.executeUpdate();
            if (deleted > 0) {
                conn.commit();
            }
            return deleted;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to truncate todo items: " + e.getMessage(), e);
        }
    }
}
