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

import org.eclipse.leshan.core.demo.cli.MultiParameterException;
import picocli.CommandLine;
import picocli.CommandLine.Help;
import picocli.CommandLine.Help.Layout;
import picocli.CommandLine.IParameterExceptionHandler;
import picocli.CommandLine.Model.ArgSpec;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Model.OptionSpec;
import picocli.CommandLine.Model.PositionalParamSpec;
import picocli.CommandLine.ParameterException;
import picocli.CommandLine.UnmatchedArgumentException;

import java.io.PrintWriter;

import static org.thingsboard.lwm2m.demo.client.util.UtilsCLI.propertyLevelCLI_debug;
import static org.thingsboard.lwm2m.demo.client.util.UtilsCLI.propertyLevelCLI;

/**
 * A Message Handler which display usage of erroneous option only, unlike the default one which display the global help
 * usage.
 *
 */
public class TBShortErrorMessageHandler implements IParameterExceptionHandler {
    @Override
    public int handleParseException(ParameterException ex, String[] args) {
        CommandLine cmd = ex.getCommandLine();
        PrintWriter writer = cmd.getErr();

        // print Error
        writer.println(cmd.getColorScheme().errorText(ex.getMessage()));
        writer.println();
        if (propertyLevelCLI_debug.equalsIgnoreCase(System.getProperty(propertyLevelCLI))) {
            writer.println(cmd.getColorScheme().stackTraceText(ex));
        }

        // print suggestions
        if (UnmatchedArgumentException.printSuggestions(ex, writer)) {
            writer.println();
        }

        // print help usage for args in error
        if (ex instanceof MultiParameterException) {
            Help help = cmd.getHelpFactory().create(cmd.getCommandSpec(), cmd.getColorScheme());
            Layout layout = help.createDefaultLayout();
            for (ArgSpec argSpec : ((MultiParameterException) ex).getArgSpecs()) {
                if (argSpec instanceof OptionSpec) {
                    layout.addOption((OptionSpec) argSpec, help.createDefaultParamLabelRenderer());
                } else if (argSpec instanceof PositionalParamSpec) {
                    layout.addPositionalParameter((PositionalParamSpec) argSpec,
                            help.createDefaultParamLabelRenderer());
                }
            }
            writer.println(layout.toString());
        } else if (ex.getArgSpec() instanceof OptionSpec) {
            Help help = cmd.getHelpFactory().create(cmd.getCommandSpec(), cmd.getColorScheme());
            Layout layout = help.createDefaultLayout();
            layout.addOption((OptionSpec) ex.getArgSpec(), help.createDefaultParamLabelRenderer());
            writer.println(layout.toString());
        } else if (ex.getArgSpec() instanceof PositionalParamSpec) {
            Help help = cmd.getHelpFactory().create(cmd.getCommandSpec(), cmd.getColorScheme());
            Layout layout = help.createDefaultLayout();
            layout.addPositionalParameter((PositionalParamSpec) ex.getArgSpec(),
                    help.createDefaultParamLabelRenderer());
            writer.println(layout.toString());
        }

        // print footer
        CommandSpec spec = cmd.getCommandSpec();
        writer.printf("Try '%s --help' for more information.%n", spec.qualifiedName());

        return cmd.getExitCodeExceptionMapper() != null ? cmd.getExitCodeExceptionMapper().getExitCode(ex)
                : spec.exitCodeOnInvalidInput();
    }
}
