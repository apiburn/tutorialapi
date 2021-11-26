package com.tutorialapi.db.service.sqlite;

import com.tutorialapi.db.exception.ConflictException;
import com.tutorialapi.db.service.TodoListService;
import com.tutorialapi.model.TodoList;
import com.tutorialapi.model.user.RapidApiPrincipal;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SqliteTodoListService implements TodoListService {
    private final DataSource dataSource;

    public SqliteTodoListService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Optional<TodoList> get(RapidApiPrincipal principal, String id) {
        String sql = "SELECT * FROM todo_lists WHERE user_id = ? AND id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            int index = 0;
            ps.setString(++index, principal.getUser());
            ps.setString(++index, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new TodoList().setId(rs.getString("id")).setName(rs.getString("name")));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch list: " + e.getMessage(), e);
        }
    }

    @Override
    public List<TodoList> getAll(RapidApiPrincipal principal) {
        String sql = "SELECT * FROM todo_lists WHERE user_id = ? ORDER BY name";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, principal.getUser());
            List<TodoList> todoLists = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    todoLists.add(new TodoList().setId(rs.getString("id")).setName(rs.getString("name")));
                }
            }
            return todoLists;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch lists: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean create(RapidApiPrincipal principal, TodoList todoList) {
        String sql = "INSERT INTO todo_lists (user_id, id, name) VALUES (?, ?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            int index = 0;
            ps.setString(++index, principal.getUser());
            ps.setString(++index, todoList.getId());
            ps.setString(++index, todoList.getName());
            if (ps.executeUpdate() > 0) {
                conn.commit();
                return true;
            }
            return false;
        } catch (SQLException e) {
            if (e.getMessage() != null && e.getMessage().contains("SQLITE_CONSTRAINT_PRIMARYKEY")) {
                throw new ConflictException("Todo list already exists");
            }
            throw new RuntimeException("Failed to create list: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean update(RapidApiPrincipal principal, TodoList todoList) {
        String sql = "UPDATE todo_lists SET name = ? WHERE user_id = ? AND id = ? AND name != ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            int index = 0;
            ps.setString(++index, todoList.getName());
            ps.setString(++index, principal.getUser());
            ps.setString(++index, todoList.getId());
            ps.setString(++index, todoList.getName());
            if (ps.executeUpdate() > 0) {
                conn.commit();
                return true;
            }
            return false;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update list: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<TodoList> delete(RapidApiPrincipal principal, String id) {
        Optional<TodoList> fetched = get(principal, id);
        if (fetched.isPresent()) {
            String sql = "DELETE FROM todo_lists WHERE user_id = ? AND id = ?";
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                int index = 0;
                ps.setString(++index, principal.getUser());
                ps.setString(++index, id);
                if (ps.executeUpdate() > 0) {
                    conn.commit();
                }
            } catch (SQLException e) {
                throw new RuntimeException("Failed to delete list: " + e.getMessage(), e);
            }
        }
        return fetched;
    }

    @Override
    public int truncate() {
        String sql = "DELETE FROM todo_lists";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            int deleted = ps.executeUpdate();
            if (deleted > 0) {
                conn.commit();
            }
            return deleted;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to truncate todo lists: " + e.getMessage(), e);
        }
    }
}
