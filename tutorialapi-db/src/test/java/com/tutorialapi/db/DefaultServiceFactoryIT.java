package com.tutorialapi.db;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.sql.DataSource;

@ExtendWith(DataSourceExtension.class)
public class DefaultServiceFactoryIT {
    private final DataSource dataSource;

    public DefaultServiceFactoryIT(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Test
    public void test() {
        ServiceFactory serviceFactory = new DefaultServiceFactory(dataSource);
        Assertions.assertNotNull(serviceFactory.getTodoListService());
        Assertions.assertNotNull(serviceFactory.getTodoItemService());
    }
}
