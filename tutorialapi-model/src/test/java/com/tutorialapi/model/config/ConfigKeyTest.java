package com.tutorialapi.model.config;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ConfigKeyTest {
    @Test
    public void testGetKey() {
        Assertions.assertEquals("server.keystore.file", ConfigKey.SERVER_KEYSTORE_FILE.getKey());
    }
}
