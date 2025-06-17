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
package org.thingsboard.lwm2m.demo.client;

import org.eclipse.leshan.core.demo.LeshanProperties;
import org.thingsboard.lwm2m.demo.client.cli.AppProperties;
import picocli.CommandLine;

public class VersionProvider implements CommandLine.IVersionProvider {

    @Override
    public String[] getVersion() throws Exception {
        LeshanProperties leshanProperties = new LeshanProperties();
        leshanProperties.load();
        AppProperties appProperties = new AppProperties();
        appProperties.load();


        return new String[]{ //
                String.format("@|italic,bold App Name:|@ @|bold    %s|@ @|bold,yellow v%s|@", appProperties.getAppName(), appProperties.getVersion()), //
                String.format("@|italic,bold Code Source: %s|@", getCodeURL()), //
                String.format("@|italic,bold Build Date: |@ @|bold %s |@", appProperties.getTimestamp()), //
                "", //
                String.format("Leshan Client @|bold,yellow v%s|@", leshanProperties.getVersion()), //
                "JVM: ${java.version} (${java.vendor} ${java.vm.name} ${java.vm.version})", //
                "OS: ${os.name} ${os.version} ${os.arch}"};
    }

    public String getCodeURL() {
        return "https://github.com/thingsboard/thingsboard.lwm2m.demo.client";
    }
}
