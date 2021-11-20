package com.tutorialapi.server;

import org.eclipse.jetty.server.HttpConfiguration;

import static org.eclipse.jetty.http.HttpScheme.HTTPS;

public class TutorialApiServer {
    public static void main(String... args) {
        System.out.println("Hello World!");

        HttpConfiguration httpsConfiguration = new HttpConfiguration();
        httpsConfiguration.setSecureScheme(HTTPS.asString());
    }
}
