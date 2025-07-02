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
import jline.console.completer.ArgumentCompleter;
import org.eclipse.leshan.core.util.StringUtils;
import org.jline.reader.LineReader;
import picocli.CommandLine;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public class TBConsoleAppenderCLI extends ConsoleAppender<ILoggingEvent> implements IConsoleAppenderCLI {

    private volatile LineReader reader;
    private volatile CommandLine commandLine;
    private final List<String> bufferedLogs = new CopyOnWriteArrayList<>();


    private volatile long timeoutMillis = 5000;
    private volatile long lastLogTimestamp = System.currentTimeMillis();

    private ScheduledExecutorService scheduler;

    @Override
    public void setLineReader(LineReader reader) {
        this.reader = reader;
    }
    @Override
    public void setCommandLine(CommandLine commandLine) {
        this.commandLine = commandLine;
    }

    @Override
    public void setTimeoutSeconds(Integer timeoutSeconds) {
        this.timeoutMillis = timeoutSeconds * 1000L;
    }

    @Override
    public void startScheduled() {
        super.start();
        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> {
            try {
                tickTimeout();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 1, 1, TimeUnit.SECONDS);
    }

    @Override
    public void stopScheduled() {
        if (scheduler != null) {
            scheduler.shutdownNow();
            scheduler = null;
        }
        super.stop();
    }

    @Override
    protected void append(ILoggingEvent event) {
        String message = new String(encoder.encode(event)).trim();
        if (reader != null) {
            bufferedLogs.add(message);
        } else {
            System.out.println(message);
        }
    }

    @Override
    public synchronized void flushBufferedLogs(String line) {
        flushBufferedLogs();
        if (this.commandLine != null) {
            try {
                if (!StringUtils.isEmpty(line)) {
                    ArgumentCompleter.ArgumentList list = new ArgumentCompleter.WhitespaceArgumentDelimiter().delimit(line, line.length());
                    String lineCommand = "\u001B[32m" + "command> " + line + "\u001B[0m";
                    this.reader.printAbove(lineCommand);
                    commandLine.execute(list.getArguments());
                }
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
            }
        }
    }

    private synchronized void flushBufferedLogs() {
        if (bufferedLogs.isEmpty()) {
            return;
        }

        if (reader != null) {
            // Завжди відступаємо перед логами, щоб не затирати введення
            System.out.println(); // або reader.getTerminal().writer().println();

            for (String msg : bufferedLogs) {
                try {
                    reader.printAbove(msg);
                } catch (Exception e) {
                    System.err.println(msg);
                }
            }

            try {
                reader.callWidget(LineReader.REDRAW_LINE);
                reader.callWidget(LineReader.REDISPLAY);
            } catch (Exception ignored) {}
        } else {
            for (String msg : bufferedLogs) {
                System.out.println(msg);
            }
        }

        bufferedLogs.clear();
        lastLogTimestamp = System.currentTimeMillis();
    }



    private synchronized void tickTimeout() {
        long now = System.currentTimeMillis();
        if ((now - lastLogTimestamp) >= timeoutMillis) {
            flushBufferedLogs();
        }
    }
}
