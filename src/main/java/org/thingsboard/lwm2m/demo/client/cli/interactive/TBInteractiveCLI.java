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

import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import jline.console.completer.ArgumentCompleter.ArgumentList;
import jline.console.completer.ArgumentCompleter.WhitespaceArgumentDelimiter;
import picocli.CommandLine;


import static org.thingsboard.lwm2m.demo.client.util.UtilsCLI.getParserCLI;
import static org.thingsboard.lwm2m.demo.client.util.UtilsCLI.getTerminalCLI;

public class TBInteractiveCLI {

//    private ConsoleReader consoleReader;
    private CommandLine commandLine;
    private final String prompt = "prompt> ";

    private LineReader reader;

    public TBInteractiveCLI(TBInteractiveCommands interactivesCommands) throws IOException {
        // set up the completion
        this.commandLine = new CommandLine(interactivesCommands);
        Completer completer = new picocli.shell.jline3.PicocliJLineCompleter(commandLine.getCommandSpec());
        this.reader = LineReaderBuilder.builder().terminal(getTerminalCLI()).completer(completer).parser(getParserCLI()).build();
        interactivesCommands.setPrintWriter(this.reader);
        interactivesCommands.setCommandLine(commandLine);
    }

    public void showHelp() {
        commandLine.usage(commandLine.getOut());
    }

    public void start() {

        // start the shell and process input until the user quits with Ctl-D
        String line;
        while ((line = this.reader.readLine(this.prompt)) != null) {
            ArgumentList list = new WhitespaceArgumentDelimiter().delimit(line, line.length());
            commandLine.execute(list.getArguments());
            this.reader.zeroOut();
        }
    }
}
