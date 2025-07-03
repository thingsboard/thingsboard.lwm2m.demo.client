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
package org.thingsboard.lwm2m.demo.client.cli.interactive;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import picocli.CommandLine;

@Component
public class TBAppVersionProviderCli implements CommandLine.IVersionProvider {

    private final TBAppInfoProperties info;

    @Autowired
    public TBAppVersionProviderCli(TBAppInfoProperties info) {
        this.info = info;
    }

    @Override
    public String[] getVersion() {
        return new String[] {
                "Name " + info.getTitle(),
                "Version " + info.getVersion(),
                "BuildTime " + info.getBuild().getTime()
        };
    }
}
