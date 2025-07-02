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

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.LogbackException;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;
import ch.qos.logback.core.status.Status;
import org.jline.reader.LineReader;
import picocli.CommandLine;

import java.util.List;
import java.util.regex.Pattern;

public interface IConsoleAppenderCLI extends Appender<ILoggingEvent> {

    void setLineReader(LineReader reader);

    void setCommandLine(CommandLine commandLine);

    void setTimeoutSeconds(Integer timeoutSeconds);

    void startScheduled();

    void stopScheduled();

    void flushBufferedLogs(String line);

    @Override
    default String getName() {
        return "";
    }

    @Override
    default void doAppend(ILoggingEvent event) throws LogbackException {

    }

    @Override
    default void setName(String name) {

    }

    @Override
    default void setContext(Context context) {

    }

    @Override
    default Context getContext() {
        return null;
    }

    @Override
    default void addStatus(Status status) {

    }

    @Override
    default void addInfo(String msg) {

    }

    @Override
    default void addInfo(String msg, Throwable ex) {

    }

    @Override
    default void addWarn(String msg) {

    }

    @Override
    default void addWarn(String msg, Throwable ex) {

    }

    @Override
    default void addError(String msg) {

    }

    @Override
    default void addError(String msg, Throwable ex) {

    }

    @Override
    default void addFilter(Filter<ILoggingEvent> newFilter) {

    }

    @Override
    default void clearAllFilters() {

    }

    @Override
    default List<Filter<ILoggingEvent>> getCopyOfAttachedFiltersList() {
        return List.of();
    }

    @Override
    default FilterReply getFilterChainDecision(ILoggingEvent event) {
        return null;
    }

    @Override
    default void start() {

    }

    @Override
    default void stop() {

    }

    @Override
    default boolean isStarted() {
        return false;
    }
}
