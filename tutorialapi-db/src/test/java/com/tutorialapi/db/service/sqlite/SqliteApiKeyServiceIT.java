package com.tutorialapi.db.service.sqlite;

import com.tutorialapi.db.DataSourceExtension;
import com.tutorialapi.db.exception.ConflictException;
import com.tutorialapi.model.user.ApiKey;
import com.tutorialapi.model.user.Subscription;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.sql.DataSource;
import java.util.Optional;

@ExtendWith(DataSourceExtension.class)
public class SqliteApiKeyServiceIT {
    private final SqliteApiKeyService apikeyService;

    public SqliteApiKeyServiceIT(DataSource dataSource) {
        this.apikeyService = new SqliteApiKeyService(dataSource);
    }

    @Test
    public void testGetMissing() {
        Optional<ApiKey> fetched = apikeyService.get("key");
        Assertions.assertTrue(fetched.isEmpty());
    }

    @Test
    public void testGetFound() {
        ApiKey apikey = new ApiKey().setApikey("key").setUser("user").setSubscription(Subscription.BASIC);
        Assertions.assertTrue(apikeyService.create(apikey));

        Optional<ApiKey> fetched = apikeyService.get(apikey.getApikey());
        Assertions.assertTrue(fetched.isPresent());
        Assertions.assertEquals(apikey, fetched.get());
    }

    @Test
    public void testCreateConflict() {
        ApiKey apikey = new ApiKey().setApikey("key").setUser("user").setSubscription(Subscription.BASIC);
        Assertions.assertTrue(apikeyService.create(apikey));
        ConflictException exception = Assertions.assertThrows(ConflictException.class,
                () -> apikeyService.create(apikey));

        Assertions.assertEquals("ApiKey already exists", exception.getMessage());
    }

    @Test
    public void testCreateConflictDifferentUser() {
        ApiKey apikey1 = new ApiKey().setApikey("key").setUser("user1").setSubscription(Subscription.BASIC);
        ApiKey apikey2 = new ApiKey().setApikey("key").setUser("user2").setSubscription(Subscription.BASIC);
        Assertions.assertTrue(apikeyService.create(apikey1));
        ConflictException exception = Assertions.assertThrows(ConflictException.class,
                () -> apikeyService.create(apikey2));

        Assertions.assertEquals("ApiKey already exists", exception.getMessage());
    }

    @Test
    public void testUpdateMissing() {
        ApiKey apikey = new ApiKey().setApikey("key").setUser("user").setSubscription(Subscription.BASIC);
        Assertions.assertFalse(apikeyService.update(apikey));
    }

    @Test
    public void testUpdateExistsButSame() {
        ApiKey apikey = new ApiKey().setApikey("key").setUser("user").setSubscription(Subscription.BASIC);
        Assertions.assertTrue(apikeyService.create(apikey));
        Assertions.assertFalse(apikeyService.update(apikey));
    }

    @Test
    public void testUpdateDifferentUser() {
        ApiKey apikey = new ApiKey().setApikey("key").setUser("user").setSubscription(Subscription.BASIC);
        Assertions.assertTrue(apikeyService.create(apikey));
        apikey.setUser("updated");
        Assertions.assertTrue(apikeyService.update(apikey));

        Optional<ApiKey> fetched = apikeyService.get(apikey.getApikey());
        Assertions.assertTrue(fetched.isPresent());
        Assertions.assertEquals(apikey, fetched.get());
    }

    @Test
    public void testUpdateDifferentSubscription() {
        ApiKey apikey = new ApiKey().setApikey("key").setUser("user").setSubscription(Subscription.BASIC);
        Assertions.assertTrue(apikeyService.create(apikey));
        apikey.setSubscription(Subscription.PRO);
        Assertions.assertTrue(apikeyService.update(apikey));

        Optional<ApiKey> fetched = apikeyService.get(apikey.getApikey());
        Assertions.assertTrue(fetched.isPresent());
        Assertions.assertEquals(apikey, fetched.get());
    }

    @Test
    public void testDeleteMissing() {
        Optional<ApiKey> deleted = apikeyService.delete("missing");
        Assertions.assertFalse(deleted.isPresent());
    }

    @Test
    public void testDeleteSuccess() {
        ApiKey apikey = new ApiKey().setApikey("key").setUser("user").setSubscription(Subscription.BASIC);
        Assertions.assertTrue(apikeyService.create(apikey));
        Optional<ApiKey> deleted = apikeyService.delete(apikey.getApikey());
        Assertions.assertTrue(deleted.isPresent());

        Optional<ApiKey> fetched = apikeyService.get(apikey.getApikey());
        Assertions.assertTrue(fetched.isEmpty());
    }

    @Test
    public void testTruncateNone() {
        int deleted = apikeyService.truncate();
        Assertions.assertEquals(0, deleted);
    }

    @Test
    public void testTruncateWithSome() {
        ApiKey apikey1 = new ApiKey().setApikey("key1").setUser("user").setSubscription(Subscription.BASIC);
        ApiKey apikey2 = new ApiKey().setApikey("key2").setUser("user").setSubscription(Subscription.BASIC);
        Assertions.assertTrue(apikeyService.create(apikey1));
        Assertions.assertTrue(apikeyService.create(apikey2));

        int deleted = apikeyService.truncate();
        Assertions.assertEquals(2, deleted);
    }
}
