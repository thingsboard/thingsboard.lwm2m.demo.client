/*******************************************************************************
 * Copyright (c) 2021 Sierra Wireless and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * and Eclipse Distribution License v1.0 which accompany this distribution.
 *
 * The Eclipse Public License is available at
 *    http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *    http://www.eclipse.org/org/documents/edl-v10.html.
 *
 * Contributors:
 *     Sierra Wireless - initial API and implementation
 *******************************************************************************/
package org.thingsboard.lwm2m.demo.client;

import org.eclipse.leshan.core.demo.LeshanProperties;
import org.thingsboard.lwm2m.demo.client.cli.AppProperties;
import picocli.CommandLine.IVersionProvider;

public class VersionProvider implements IVersionProvider {

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
