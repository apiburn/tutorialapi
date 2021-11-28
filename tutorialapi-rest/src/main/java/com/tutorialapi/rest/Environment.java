package com.tutorialapi.rest;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.tutorialapi.db.DefaultServiceFactory;
import com.tutorialapi.db.ServiceFactory;
import com.tutorialapi.model.config.ConfigKey;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class Environment {
    private static final Logger LOGGER = LoggerFactory.getLogger(Environment.class);

    private static final String CONFIG_URL_PATTERN =
        "https://raw.githubusercontent.com/apiburn/tutorialapi/main/system-%s.properties";

    private final Config config;
    private final ServiceFactory serviceFactory;

    public Environment(Config config, ServiceFactory serviceFactory) {
        this.config = config;
        this.serviceFactory = serviceFactory;
    }

    private static ServiceFactory createServiceFactory(Config config) {
        HikariConfig dbConfig = new HikariConfig();
        dbConfig.setDriverClassName(config.getString(ConfigKey.DB_DRIVER.getKey()));
        dbConfig.setJdbcUrl(config.getString(ConfigKey.DB_URL.getKey()));
        dbConfig.setUsername(config.getString(ConfigKey.DB_USERNAME.getKey()));
        dbConfig.setPassword(config.getString(ConfigKey.DB_PASSWORD.getKey()));
        dbConfig.setAutoCommit(false);

        DataSource dataSource = new HikariDataSource(dbConfig);
        return new DefaultServiceFactory(dataSource);
    }

    private static boolean hasChanged(Config oldConfig, Config newConfig) {
        String oldConfigStr = oldConfig.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> entry.getKey() + " => " + entry.getValue().render())
                .collect(Collectors.joining("\n"));
        String newConfigStr = newConfig.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> entry.getKey() + " => " + entry.getValue().render())
                .collect(Collectors.joining("\n"));
        return !oldConfigStr.equals(newConfigStr);
    }

    private static final AtomicReference<Environment> environmentRef = new AtomicReference<>();

    public static Supplier<Environment> createEnvironment(Executor executor, String mode) {
        LoadingCache<Boolean, Environment> loadingCache = CacheBuilder.newBuilder()
                .expireAfterWrite(2, TimeUnit.MINUTES)
                .build(CacheLoader.asyncReloading(CacheLoader.from(() -> {
                    try {
                        String url = String.format(CONFIG_URL_PATTERN, mode);
                        Config config = ConfigFactory.parseURL(new URL(url));

                        boolean loadEnvironment = false;
                        if (environmentRef.get() == null) {
                            LOGGER.info("Creating environment");
                            loadEnvironment = true;
                        } else if (hasChanged(environmentRef.get().getConfig(), config)) {
                            LOGGER.info("Reloading environment since config changed");
                            loadEnvironment = true;
                        }

                        if (loadEnvironment) {
                            return new Environment(config, createServiceFactory(config));
                        }
                        return environmentRef.get();
                    } catch (MalformedURLException e) {
                        throw new RuntimeException("Invalid URL", e);
                    }
                }), executor));

        return () -> {
            try {
                Environment environment = loadingCache.get(true);
                environmentRef.set(environment);
                return environment;
            } catch (ExecutionException e) {
                LOGGER.error("Failed to create environment", e);
                throw new RuntimeException(e);
            }
        };
    }

    public Config getConfig() {
        return config;
    }

    public ServiceFactory getServiceFactory() {
        return serviceFactory;
    }
}
