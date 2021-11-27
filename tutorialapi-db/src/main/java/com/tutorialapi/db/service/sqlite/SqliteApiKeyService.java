package com.tutorialapi.db.service.sqlite;

import com.tutorialapi.db.exception.ConflictException;
import com.tutorialapi.db.service.ApiKeyService;
import com.tutorialapi.model.user.ApiKey;
import com.tutorialapi.model.user.Subscription;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class SqliteApiKeyService implements ApiKeyService {
    private final DataSource dataSource;

    public SqliteApiKeyService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Optional<ApiKey> get(String apikey) {
        String sql = "SELECT * FROM api_keys WHERE apikey = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, apikey);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Optional<Subscription> subscription = Subscription.from(rs.getString("subscription"));
                    if (subscription.isEmpty()) {
                        return Optional.empty();
                    }
                    return Optional.of(
                            new ApiKey()
                                    .setApikey(rs.getString("apikey"))
                                    .setUser(rs.getString("user_id"))
                                    .setSubscription(subscription.get())
                    );
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch api key: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean create(ApiKey apikey) {
        String sql = "INSERT INTO api_keys (apikey, user_id, subscription) VALUES (?, ?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            int index = 0;
            ps.setString(++index, apikey.getApikey());
            ps.setString(++index, apikey.getUser());
            ps.setString(++index, apikey.getSubscription().name());
            if (ps.executeUpdate() > 0) {
                conn.commit();
                return true;
            }
            return false;
        } catch (SQLException e) {
            if (e.getMessage() != null && e.getMessage().contains("SQLITE_CONSTRAINT_PRIMARYKEY")) {
                throw new ConflictException("ApiKey already exists");
            }
            throw new RuntimeException("Failed to create api key: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean update(ApiKey apikey) {
        String sql = "UPDATE api_keys SET user_id = ?, subscription = ? " +
                "WHERE apikey = ? AND (user_id != ? OR subscription != ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            int index = 0;
            ps.setString(++index, apikey.getUser());
            ps.setString(++index, apikey.getSubscription().name());
            ps.setString(++index, apikey.getApikey());
            ps.setString(++index, apikey.getUser());
            ps.setString(++index, apikey.getSubscription().name());
            if (ps.executeUpdate() > 0) {
                conn.commit();
                return true;
            }
            return false;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update api key: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<ApiKey> delete(String key) {
        Optional<ApiKey> fetched = get(key);
        if (fetched.isPresent()) {
            String sql = "DELETE FROM api_keys WHERE apikey = ?";
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, key);
                if (ps.executeUpdate() > 0) {
                    conn.commit();
                }
            } catch (SQLException e) {
                throw new RuntimeException("Failed to delete api key: " + e.getMessage(), e);
            }
        }
        return fetched;
    }

    @Override
    public int truncate() {
        String sql = "DELETE FROM api_keys";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            int deleted = ps.executeUpdate();
            if (deleted > 0) {
                conn.commit();
            }
            return deleted;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to truncate api keys: " + e.getMessage(), e);
        }
    }
}
