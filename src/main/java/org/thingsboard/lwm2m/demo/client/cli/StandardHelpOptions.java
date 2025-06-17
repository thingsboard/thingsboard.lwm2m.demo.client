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

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import org.slf4j.LoggerFactory;
import picocli.CommandLine.Option;

/**
 * Mixing about stand helps Options.
 */
public class StandardHelpOptions {

    @Option(names = { "-h", "--help" }, description = "Display help information.", usageHelp = true)
    private boolean help;

    @Option(names = { "-V", "--version" }, description = "Print version information and exit.", versionHelp = true)
    private boolean versionRequested;

    private int verboseLevel = 0;

    @Option(names = { "-v", "--verbose" },
            description = { "Specify multiple -v options to increase verbosity.", //
                    "For example, `-v -v -v` or `-vvv`", //
                    "", //
                    "You can adjust more precisely log output using logback configuration file," + //
                            " see 'How to activate more log ?' in FAQ:", //
                    "  https://github.com/eclipse/leshan/wiki/F.A.Q./" })
    public void setVerbose(boolean[] verbose) {
        verboseLevel = verbose.length;

        // set CLI verbosity. (See ShortErrorMessageHandler)
        if (verbose.length > 0) {
            System.setProperty("leshan.cli", "DEBUG");
        }

        // change application log level.
        if (verbose.length > 0) {
            LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
            switch (verbose.length) {
            case 1:
                setLogLevel(loggerContext, "org.eclipse.leshan", Level.INFO);
                setLogLevel(loggerContext, "org.eclipse.californium", Level.INFO);
                break;
            case 2:
                setLogLevel(loggerContext, "org.eclipse.leshan", Level.DEBUG);
                setLogLevel(loggerContext, "org.eclipse.californium", Level.DEBUG);
                break;
            case 3:
                setLogLevel(loggerContext, "org.eclipse.leshan", Level.TRACE);
                setLogLevel(loggerContext, "org.eclipse.californium", Level.TRACE);
                break;
            case 4:
                setLogLevel(loggerContext, "org.eclipse.leshan", Level.TRACE);
                setLogLevel(loggerContext, "org.eclipse.californium", Level.TRACE);
                setLogLevel(loggerContext, Logger.ROOT_LOGGER_NAME, Level.TRACE);
                break;
            }
        }
    }

    private void setLogLevel(LoggerContext loggerContext, String loggerName, Level level) {
        Logger logger = loggerContext.getLogger(loggerName);
        if (logger != null)
            logger.setLevel(level);
    }

    public int getVerboseLevel() {
        return verboseLevel;
    }
}
