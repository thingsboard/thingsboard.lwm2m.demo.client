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

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.thingsboard.lwm2m.demo.client.cli.interactive.TBInteractiveCommands;
import org.thingsboard.lwm2m.demo.client.cli.interactive.TBInteractiveCLI;
import org.thingsboard.lwm2m.demo.client.core.LwM2MClient;
import org.thingsboard.lwm2m.demo.client.service.LwM2MClientService;
import org.eclipse.leshan.client.LeshanClient;
import org.eclipse.leshan.core.model.LwM2mModelRepository;
import picocli.CommandLine;

import java.io.PrintWriter;

import static org.thingsboard.lwm2m.demo.client.util.Utils.createModel;

@Component
public class TBDemoCliRunnerImpl implements CommandLineRunner {

    private final LwM2MClient lwM2MClient;
    private final LwM2MClientService lwM2MClientService;

    public TBDemoCliRunnerImpl(LwM2MClient lwM2MClient, LwM2MClientService lwM2MClientService) {
        this.lwM2MClient = lwM2MClient;
        this.lwM2MClientService = lwM2MClientService;
    }

    @Override
    public void run(String... args) {
        TBSectionsCliMain cli = new TBSectionsCliMain();
        CommandLine command = new CommandLine(cli).setParameterExceptionHandler(new TBShortErrorMessageHandler());
        int exitCode = command.execute(args);
        if (exitCode != 0 || command.isUsageHelpRequested() || command.isVersionHelpRequested()) {
            System.exit(exitCode);
        }
        try {
            LwM2mModelRepository repository = createModel(cli);
            LeshanClient client = lwM2MClient.create(cli, repository);
            if (cli.main.interactiveConsole) {
                // Print commands help
                TBInteractiveCLI tbInteractiveCLI = new TBInteractiveCLI(new TBInteractiveCommands(client, repository), cli);
//                tbInteractiveCLI.showHelp();
                // Start the client
                lwM2MClientService.start(client);
                // Start interactive console
                tbInteractiveCLI.run();
            } else {
                // Start the client without Interactive console
                lwM2MClientService.start(client);
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