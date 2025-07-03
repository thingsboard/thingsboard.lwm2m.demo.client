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

import java.io.PrintWriter;

import org.jline.reader.LineReader;
import picocli.CommandLine;

public class TBJLineInteractiveCommands {

    private PrintWriter out;
    private CommandLine commandLine;

    void setPrintWriter(LineReader reader) {
        out = reader.getTerminal().writer();
    }

    void setCommandLine(CommandLine commandLine) {
        this.commandLine = commandLine;
    }

    public PrintWriter getConsoleWriter() {
        return out;
    }

    /**
     * Print Help Usage in the console.
     */
    public void printUsageMessage() {
        out.print(commandLine.getUsageMessage());
        out.flush();
    }

    /**
     * A convenience method to write a formatted string to this writer using the specified format string and arguments.
     * <p>
     * See {@link PrintWriter#printf(String, Object...)}
     */
    public PrintWriter printf(String format, Object... args) {
        String formatted = String.format(format, args); // %s, %d
        String colored = CommandLine.Help.Ansi.ON.string(formatted); // обробляє @|...|@
        return out.printf("%s", colored);
    }

    /**
     * A convenience method to write a formatted string to this writer using the specified format string and arguments.
     * <p>
     * This function support ANSI color tag, see https://picocli.info/#_usage_help_with_styles_and_colors
     * <p>
     * See {@link PrintWriter#printf(String, Object...)}
     */
    public PrintWriter printfAnsi(String format, Object... args) {
        String formatted = String.format(format, args); // %s, %d
        String colored = CommandLine.Help.Ansi.ON.string(formatted); // action @|...|@
        return out.printf("%s", colored);
    }

    /**
     * A convenience method to write a formatted string to this writer using the specified format string and arguments.
     * <p>
     * The error style from the {@link CommandLine#getColorScheme()} will be used.
     * <p>
     * See {@link PrintWriter#printf(String, Object...)}
     */
    public PrintWriter printfError(String string, Object... args) {
        out.printf(commandLine.getColorScheme().errorText(string).toString(), args);
        return out;
    }

    /**
     * Flush the stream on the console output.
     */
    public void flush() {
        out.flush();
    }
}
