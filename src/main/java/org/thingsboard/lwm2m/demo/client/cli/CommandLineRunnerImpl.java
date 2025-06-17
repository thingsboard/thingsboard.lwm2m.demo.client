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

import org.eclipse.leshan.core.demo.cli.interactive.InteractiveCLI;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.thingsboard.lwm2m.demo.client.cli.interactive.InteractiveCommands;
import org.thingsboard.lwm2m.demo.client.core.ClientFactory;
import org.thingsboard.lwm2m.demo.client.service.LwM2mClientService;
import org.eclipse.leshan.client.LeshanClient;
import org.eclipse.leshan.core.model.LwM2mModelRepository;
import picocli.CommandLine;

import java.io.PrintWriter;

import static org.thingsboard.lwm2m.demo.client.util.Utils.createModel;

@Component
public class CommandLineRunnerImpl implements CommandLineRunner {

    private final ClientFactory clientFactory;
    private final LwM2mClientService clientService;

    public CommandLineRunnerImpl(ClientFactory clientFactory, LwM2mClientService clientService) {
        this.clientFactory = clientFactory;
        this.clientService = clientService;
    }

    @Override
    public void run(String... args) {
        ClientDemoCLI cli = new ClientDemoCLI();
        CommandLine command = new CommandLine(cli).setParameterExceptionHandler(new ShortErrorMessageHandler());

        int exitCode = command.execute(args);
        if (exitCode != 0 || command.isUsageHelpRequested() || command.isVersionHelpRequested()) {
            System.exit(exitCode);
        }

        try {
            LwM2mModelRepository repository = createModel(cli);
            LeshanClient client = clientFactory.create(cli, repository);
            if (cli.helpsOptions.getVerboseLevel() > 0) {
                // Print commands help
                InteractiveCLI console = new InteractiveCLI(new InteractiveCommands(client, repository));
                console.showHelp();
                // Start the client
                clientService.start(client);
                // Start interactive console
                console.start();
            } else {
                // Start the client without Interactive console
                clientService.start(client);
            }
        } catch (Exception e) {
            PrintWriter printer = command.getErr();
            printer.print(command.getColorScheme().errorText("Unable to create and start client ..."));
            printer.printf("%n%n");
            printer.print(command.getColorScheme().stackTraceText(e));
            printer.flush();
            System.exit(1);
        }
    }
}