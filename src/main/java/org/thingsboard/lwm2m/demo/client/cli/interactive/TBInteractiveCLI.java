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

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.impl.history.DefaultHistory;
import org.slf4j.LoggerFactory;
import org.thingsboard.lwm2m.demo.client.cli.TBSectionsCliMain;
import picocli.CommandLine;
import static org.thingsboard.lwm2m.demo.client.util.UtilsCLI.getParserCLI;
import static org.thingsboard.lwm2m.demo.client.util.UtilsCLI.getTerminalCLI;

public class TBInteractiveCLI {

    private final CommandLine commandLine;
    private final String prompt = "prompt> ";
    private final LineReader reader;
    private final TBSectionsCliMain cli;
    private final Appender<ILoggingEvent> appenderCLI;
    private final TBSectionCliInteractiveCommands interactiveCommands;


    public TBInteractiveCLI(TBSectionCliInteractiveCommands interactiveCommands, TBSectionsCliMain cli) throws IOException {
        // Set up CLI with completion
        this.interactiveCommands = interactiveCommands;
        this.commandLine = new CommandLine(this.interactiveCommands);

        this.cli = cli;
        Completer completer = new picocli.shell.jline3.PicocliJLineCompleter(commandLine.getCommandSpec());
        this.reader = LineReaderBuilder.builder()
                .terminal(getTerminalCLI())
                .completer(completer)
                .parser(getParserCLI())
                .history(new DefaultHistory())
                .build();
        interactiveCommands.setPrintWriter(this.reader);
        interactiveCommands.setCommandLine(commandLine);
        this.appenderCLI = getAppenderCLI();
        propagateLineReaderToAppenders();
    }

    /**
     * ANSI Text Color Codes
     * ---------------------
     * Black       = \u001B[30m
     * Red         = \u001B[31m
     * Green       = \u001B[32m
     * Yellow      = \u001B[33m
     * Blue        = \u001B[34m
     * Magenta     = \u001B[35m
     * Cyan        = \u001B[36m
     * White       = \u001B[37m
     *
     * Bright/Bold Modifier: \u001B[1m
     * Reset (normal):       \u001B[0m
     *
     * Examples:
     *   System.out.println("\u001B[32mGreen Text\u001B[0m");
     *   System.out.println("\u001B[1;31mBold Red Text\u001B[0m");
     */
    public void showHelp() {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        // help-текст
        commandLine.usage(pw);
        pw.flush();
        String helpGreenText = "\u001B[34m" + sw + "\u001B[0m";
        if (this.reader != null) {
            this.reader.printAbove(helpGreenText); // якщо інтерактивний режим
        } else {
            System.out.println(helpGreenText); // звичайний консольний вивід
        }

    }
    public void run() {
        reader.printAbove("\u001B[34mInteractive Commands started, timeout: [" + this.cli.main.cliTimeoutSeconds + "] sec.\u001B[0m");
        this.showHelp();
        this.interactiveCommands.setRunning(true);
        while (this.interactiveCommands.getRunning()) {
            try {
                setValueReflection("flushBufferedLogs", this.reader.readLine(this.prompt),  String.class);
            } catch (Exception e) {
                break;
            }
        }
    }

    private Appender<ILoggingEvent> getAppenderCLI() {
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        Logger rootLogger = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);
        return rootLogger.getAppender("CLI");
    }


    private void propagateLineReaderToAppenders() {
        try {
            // setLineReader
            setValueReflection("setLineReader", this.reader,  LineReader.class);
            // setCommandLine
            setValueReflection("setCommandLine", this.commandLine, CommandLine.class);
           // setCommandLine
            setValueReflection("setTimeoutSeconds", this.cli.main.cliTimeoutSeconds, Integer.class);
            // start scheduler for timeOut
            setValueReflection("startScheduled");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setValueReflection(String methodName, Object value,  Class<?> parameterTypes) {
        try{
            Method method = this.appenderCLI.getClass().getMethod(methodName, parameterTypes);
            method.invoke(this.appenderCLI, value);
        } catch (NoSuchMethodException ignored) {
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void setValueReflection(String methodName) {
        try{
            Method method = this.appenderCLI.getClass().getMethod(methodName);
            method.invoke(this.appenderCLI);
        } catch (NoSuchMethodException ignored) {
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
