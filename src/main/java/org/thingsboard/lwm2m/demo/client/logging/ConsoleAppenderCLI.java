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
package org.thingsboard.lwm2m.demo.client.logging;

import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.classic.spi.ILoggingEvent;
import static org.thingsboard.lwm2m.demo.client.util.UtilsCLI.propertyIsCLI;
import static org.thingsboard.lwm2m.demo.client.util.UtilsCLI.propertyIsCLI_true;

public class ConsoleAppenderCLI extends ConsoleAppender<ILoggingEvent> {

    @Override
    protected void append(ILoggingEvent event) {
        String message = new String(encoder.encode(event));
        if (propertyIsCLI_true.equals(System.getProperty(propertyIsCLI))) {
            System.out.println();
            System.out.print(message);
        } else {
            System.out.print(message);
        }
    }
}
