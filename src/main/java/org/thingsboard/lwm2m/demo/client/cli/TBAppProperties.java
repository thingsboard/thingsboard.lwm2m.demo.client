/**
 * Copyright © 2016-2025 The Thingsboard Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.thingsboard.lwm2m.demo.client.cli;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;



public class TBAppProperties {
    public static final String APP_VERSION = "app.version";
    public static final String APP_NAME = "app.name";
    public static final String TIMESTAMP = "app.timestamp";

    private Properties prop;

    public void load() throws IOException {
        prop = new Properties();
        try (InputStream in = getClass().getResourceAsStream("/application.yml")) {
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
