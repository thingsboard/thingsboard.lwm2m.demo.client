package org.thingsboard.lwm2m.demo.client.cli;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;



public class AppProperties {
    public static final String APP_VERSION = "app.version";
    public static final String APP_NAME = "app.name";
    public static final String TIMESTAMP = "app.timestamp";

    private Properties prop;

    public void load() throws IOException {
        prop = new Properties();
        try (InputStream in = getClass().getResourceAsStream("/application.properties")) {
            prop.load(in);
        }
    }

    public String getVersion() {
        String version = prop.getProperty(APP_VERSION);
        if (!hasRealValue(version)) {
            return "???";
        } else {
            return version;
        }
    }

    public String getAppName() {
        String commitId = prop.getProperty(APP_NAME);
        if (!hasRealValue(commitId)) {
            return "???";
        } else {
            return commitId;
        }
    }

    public String getTimestamp() {
        String timestamp = prop.getProperty(TIMESTAMP);
        if (!hasRealValue(timestamp)) {
            return "???";
        } else {
            return timestamp;
        }
    }


    private static boolean hasRealValue(String name) {
        return name != null && !(name.startsWith("${") && name.endsWith("}"));
    }
}
