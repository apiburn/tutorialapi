package com.tutorialapi.server;

import com.tutorialapi.rest.ApiApplication;
import com.tutorialapi.model.config.ConfigKey;
import com.tutorialapi.model.config.SystemKey;
import com.tutorialapi.server.task.MemoryLoggingTask;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.eclipse.jetty.http.HttpScheme;
import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.glassfish.jersey.servlet.ServletContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TutorialApiServer {
    private static final Logger LOGGER = LoggerFactory.getLogger(TutorialApiServer.class);

    private static final String ROOT_CONTEXT = "/";
    private static final String API_PATTERN = "/api/*";

    private static Server createJettyServer(int port, Config config) throws IOException {
        HttpConfiguration httpsConfiguration = new HttpConfiguration();
        httpsConfiguration.setSecureScheme(HttpScheme.HTTPS.asString());
        httpsConfiguration.setSecurePort(port);
        httpsConfiguration.addCustomizer(new SecureRequestCustomizer());
        httpsConfiguration.setSendServerVersion(false);
        httpsConfiguration.setSendDateHeader(false);

        HttpConnectionFactory httpsConnectionFactory = new HttpConnectionFactory(httpsConfiguration);

        SslContextFactory.Server sslContextFactory = new SslContextFactory.Server();
        sslContextFactory.setKeyStorePath(config.getString(ConfigKey.SERVER_KEYSTORE_FILE.getKey()));
        sslContextFactory.setKeyStoreType(config.getString(ConfigKey.SERVER_KEYSTORE_TYPE.getKey()));
        sslContextFactory.setKeyStorePassword(config.getString(ConfigKey.SERVER_KEYSTORE_PASSWORD.getKey()));
        sslContextFactory.setKeyManagerPassword(config.getString(ConfigKey.SERVER_KEYSTORE_PASSWORD.getKey()));
        sslContextFactory.setTrustAll(true);

        SslConnectionFactory sslConnectionFactory =
                new SslConnectionFactory(sslContextFactory, HttpVersion.HTTP_1_1.asString());

        Server server = new Server();

        ServerConnector httpsConnector = new ServerConnector(server, sslConnectionFactory, httpsConnectionFactory);
        httpsConnector.setPort(httpsConfiguration.getSecurePort());

        server.addConnector(httpsConnector);

        ServletContextHandler servletContextHandler = new ServletContextHandler(ServletContextHandler.NO_SESSIONS);
        servletContextHandler.setContextPath(ROOT_CONTEXT);
        servletContextHandler.setBaseResource(Resource.newResource(config.getString(ConfigKey.SERVER_WEB_CONTENT.getKey())));
        servletContextHandler.addServlet(DefaultServlet.class, ROOT_CONTEXT);

        server.setHandler(servletContextHandler);

        ServletHolder apiServletHolder = new ServletHolder(new ServletContainer(new ApiApplication(config)));
        servletContextHandler.addServlet(apiServletHolder, API_PATTERN);

        return server;
    }

    public static void main(String... args) throws Exception {
        int port = Integer.parseInt(Optional.ofNullable(System.getProperty(SystemKey.PORT.getKey()))
                .orElse(SystemKey.PORT.getDefaultValue()));
        String mode = Optional.ofNullable(System.getProperty(SystemKey.MODE.getKey()))
                .orElse(SystemKey.MODE.getDefaultValue());

        String url = String.format("https://raw.githubusercontent.com/apiburn/tutorialapi/main/system-%s.properties", mode);
        Config config = ConfigFactory.parseURL(new URL(url));

        ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        scheduledExecutorService.scheduleAtFixedRate(new MemoryLoggingTask(), 2, 2, TimeUnit.MINUTES);

        Server server = createJettyServer(port, config);

        LOGGER.info("Server starting on port: {}", port);
        server.start();
        server.join();
    }
}
