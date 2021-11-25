package com.tutorialapi.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.extension.*;
import org.sqlite.JDBC;

import javax.sql.DataSource;

public class DataSourceExtension implements BeforeAllCallback, BeforeEachCallback, ParameterResolver {
    private final DataSource dataSource;

    public DataSourceExtension() {
        HikariConfig config = new HikariConfig();
        config.setDriverClassName(JDBC.class.getName());
        config.setJdbcUrl("jdbc:sqlite::memory:");
        config.setUsername("user");
        config.setPassword("pass");
        config.setAutoCommit(false);

        dataSource = new HikariDataSource(config);
    }

    @Override
    public void beforeAll(ExtensionContext context) {
        Flyway.configure()
                .dataSource(dataSource)
                .locations("db/migration/todo")
                .load()
                .migrate();
    }

    @Override
    public void beforeEach(ExtensionContext context) {
        ServiceFactory serviceFactory = new DefaultServiceFactory(dataSource);
        serviceFactory.getTodoListService().truncate();
        serviceFactory.getTodoItemService().truncate();
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        return parameterContext.getParameter().getType().equals(DataSource.class);
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        return dataSource;
    }
}
