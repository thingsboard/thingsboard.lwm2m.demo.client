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
package org.thingsboard.lwm2m.demo.client.util;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.Appender;
import org.jline.reader.impl.DefaultParser;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.slf4j.LoggerFactory;
import org.thingsboard.lwm2m.demo.client.logging.ConsoleAppenderCLI;

import java.io.IOException;

public class UtilsCLI {

    public static final String propertyIsCLI = "cli.mode";
    public static final String propertyIsCLI_true = "true";
    public static final String propertyIsCLI_false = "false";


    public static Terminal getTerminalCLI() {
        try {
            return TerminalBuilder.builder()
    //                .jna(true) // або .jansi(true) якщо JNA не працює
                    .system(true)
                    .streams(System.in, System.out)
                    .build();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static DefaultParser getParserCLI() {
        return new DefaultParser();
    }

    public static ConsoleAppenderCLI getCLIAppender() {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        Logger rootLogger = context.getLogger(Logger.ROOT_LOGGER_NAME);

        @SuppressWarnings("unchecked")
        Appender<?> appender = rootLogger.getAppender("CLI");

        if (appender instanceof ConsoleAppenderCLI) {
            return (ConsoleAppenderCLI) appender;
        } else {
            return null;
        }
    }
}
