package com.tutorialapi.server;

import org.eclipse.jetty.server.*;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.glassfish.jersey.servlet.ServletContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.eclipse.jetty.http.HttpScheme.HTTPS;
import static org.eclipse.jetty.http.HttpVersion.HTTP_1_1;
import static org.eclipse.jetty.servlet.ServletContextHandler.NO_SESSIONS;

public class TutorialApiServer {
    private static final Logger LOGGER = LoggerFactory.getLogger(TutorialApiServer.class);

    public static void main(String... args) throws Exception {
        HttpConfiguration httpsConfiguration = new HttpConfiguration();
        httpsConfiguration.setSecureScheme(HTTPS.asString());
        httpsConfiguration.setSecurePort(8443);
        httpsConfiguration.addCustomizer(new SecureRequestCustomizer());
        httpsConfiguration.setSendServerVersion(false);
        httpsConfiguration.setSendDateHeader(false);

        HttpConnectionFactory httpsConnectionFactory = new HttpConnectionFactory(httpsConfiguration);

        SslContextFactory.Server sslContextFactory = new SslContextFactory.Server();
        sslContextFactory.setKeyStorePath("tutorialapi-server/src/main/resources/certs/tutorialapi.p12");
        sslContextFactory.setKeyStoreType("PKCS12");
        sslContextFactory.setKeyStorePassword("changeit");
        sslContextFactory.setKeyManagerPassword("changeit");
        sslContextFactory.setTrustAll(true);

        SslConnectionFactory sslConnectionFactory = new SslConnectionFactory(sslContextFactory, HTTP_1_1.asString());

        Server server = new Server();

        ServerConnector httpsConnector = new ServerConnector(server, sslConnectionFactory, httpsConnectionFactory);
        httpsConnector.setName("secure");
        httpsConnector.setPort(httpsConfiguration.getSecurePort());

        server.addConnector(httpsConnector);

        ServletContextHandler servletContextHandler = new ServletContextHandler(NO_SESSIONS);
        servletContextHandler.setContextPath("/");
        servletContextHandler.setBaseResource(Resource.newResource("tutorialapi-server/src/main/resources/www"));
        servletContextHandler.addServlet(DefaultServlet.class, "/");

        server.setHandler(servletContextHandler);

        ServletHolder apiServletHolder = servletContextHandler.addServlet(ServletContainer.class, "/api/*");
        apiServletHolder.setInitParameter("jakarta.ws.rs.Application", "TODO");

        LOGGER.info("Server starting");
        server.start();
        server.join();
    }
}
