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

import lombok.extern.slf4j.Slf4j;
import org.eclipse.leshan.client.LeshanClient;
import org.eclipse.leshan.client.resource.LwM2mInstanceEnabler;
import org.eclipse.leshan.client.resource.LwM2mObjectEnabler;
import org.eclipse.leshan.client.resource.LwM2mObjectTree;
import org.eclipse.leshan.client.resource.ObjectEnabler;
import org.eclipse.leshan.client.resource.ObjectsInitializer;
import org.eclipse.leshan.client.send.ManualDataSender;
import org.eclipse.leshan.client.send.NoDataException;
import org.eclipse.leshan.client.send.SendService;
import org.eclipse.leshan.client.servers.LwM2mServer;
import org.eclipse.leshan.core.LwM2m.Version;
import org.eclipse.leshan.core.LwM2mId;
import org.eclipse.leshan.core.demo.cli.converters.LwM2mPathConverter;
import org.eclipse.leshan.core.demo.cli.converters.StringLwM2mPathConverter;
import org.eclipse.leshan.core.demo.cli.converters.VersionConverter;
import org.eclipse.leshan.core.model.LwM2mModelRepository;
import org.eclipse.leshan.core.model.ObjectModel;
import org.eclipse.leshan.core.model.ResourceModel;
import org.eclipse.leshan.core.model.StaticModel;
import org.eclipse.leshan.core.node.LwM2mPath;
import org.eclipse.leshan.core.request.ContentFormat;
import org.eclipse.leshan.core.response.ErrorCallback;
import org.eclipse.leshan.core.response.ResponseCallback;
import org.eclipse.leshan.core.response.SendResponse;
import org.thingsboard.lwm2m.demo.client.cli.interactive.TBSectionCliInteractiveCommands.RebootCommand;
import org.thingsboard.lwm2m.demo.client.objects.MyDevice;
import org.thingsboard.lwm2m.demo.client.objects.MyLocation;
import org.thingsboard.lwm2m.demo.client.cli.interactive.TBSectionCliInteractiveCommands.CollectCommand;
import org.thingsboard.lwm2m.demo.client.cli.interactive.TBSectionCliInteractiveCommands.CreateCommand;
import org.thingsboard.lwm2m.demo.client.cli.interactive.TBSectionCliInteractiveCommands.DeleteCommand;
import org.thingsboard.lwm2m.demo.client.cli.interactive.TBSectionCliInteractiveCommands.ListCommand;
import org.thingsboard.lwm2m.demo.client.cli.interactive.TBSectionCliInteractiveCommands.MoveCommand;
import org.thingsboard.lwm2m.demo.client.cli.interactive.TBSectionCliInteractiveCommands.SendCommand;
import org.thingsboard.lwm2m.demo.client.cli.interactive.TBSectionCliInteractiveCommands.UpdateRegistrationCommand;
import org.thingsboard.lwm2m.demo.client.cli.interactive.TBSectionCliInteractiveCommands.ExitCommand;
import org.thingsboard.lwm2m.demo.client.cli.interactive.TBSectionCliInteractiveCommands.VersionCommand;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParameterException;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParentCommand;
import picocli.CommandLine.Spec;
import picocli.CommandLine.HelpCommand;
import java.util.List;
import java.util.Map;

import static org.thingsboard.lwm2m.demo.client.util.UtilsCLI.propertyLevelCLI_name;

/**
 * Interactive commands for the Thingsboard Lwm2m Demo Client
 */
@Command(name = "",
         description = "@|bold,underline Thingsboard Lwm2m Demo Client Interactive Console :|@%n",
        footer = {
                "- Press 'Ctrl-V' once to paste text.",
                "- Press 'Ctrl-C' once to copy text.",
                "- Press 'Ctrl-C' 'Ctrl-C' quickly to exit the demo client.",
                "- Use the CLI command 'stop' to pause reading input from the console without exiting the application."
        },
         subcommands = { HelpCommand.class, ListCommand.class, CreateCommand.class, DeleteCommand.class,
                 UpdateRegistrationCommand.class, SendCommand.class, CollectCommand.class, MoveCommand.class,
                 RebootCommand.class, ExitCommand.class, VersionCommand.class },
        // TODO "update"
        // TBSectionCliInteractiveCommands.UpdateResourceValues.class },
        // Readme: | `update`                                           | Update value of Resource and `send current-value`.  Note: Resource must be `RW`
        // Example: update /5/0/1=pathForUrl /3/0/14=setUtcOffset((String)

         customSynopsis = { "" },
         synopsisHeading = ""
)

@Slf4j
public class TBSectionCliInteractiveCommands extends TBJLineInteractiveCommands implements Runnable {

    private final LeshanClient client;
    private final LwM2mModelRepository repository;
    private final TBAppVersionProviderCli TBAppVersionProviderCli;
    private boolean running;

    public TBSectionCliInteractiveCommands(LeshanClient client, LwM2mModelRepository repository, TBAppVersionProviderCli TBAppVersionProviderCli) {
        this.client = client;
        this.repository = repository;
        this.TBAppVersionProviderCli = TBAppVersionProviderCli;
    }

    @Override
    public void run() {
        printUsageMessage();
    }

    public void setRunning(boolean running) {
        this.running = running;
    }
    public boolean getRunning() {
        return this.running ;
    }

    public String[] getAppVersion() {
        // повернути версію, наприклад, звертаючись до TBAppVersionProviderCli
        return this.TBAppVersionProviderCli.getVersion();
    }

    /**
     * A command to discover objects.
     */
    @Command(name = "discover",
             description = "List available Objects, Instances and Resources",
             headerHeading = "%n",
             footer = "")
    static class ListCommand implements Runnable {

        @Parameters(description = "Id of the object, if no value is specified all available objects will be listed.",
                    index = "0",
                    arity = "0..1")
        private Integer objectId;

        @ParentCommand
        TBSectionCliInteractiveCommands parent;

        @Override
        public void run() {
            LwM2mObjectTree objectTree = parent.client.getObjectTree();
            if (objectTree == null) {
                parent.printf("no object.%n");
                parent.flush();
                return;
            }
            if (objectId != null) {
                // print object with given id
                LwM2mObjectEnabler objectEnabler = objectTree.getObjectEnablers().get(objectId);
                if (objectEnabler == null) {
                    parent.printf("no object available with id %d.%n", objectId);
                    parent.flush();
                    return;
                }
                printObject(objectEnabler);
                parent.flush();
            } else {
                // print all objects
                objectTree.getObjectEnablers().forEach((objectId, objectEnabler) -> {
                    printObject(objectEnabler);
                });
                parent.flush();
            }
        }

        public void printObject(LwM2mObjectEnabler objectEnabler) {
            ObjectModel objectModel = objectEnabler.getObjectModel();
            objectEnabler.getAvailableInstanceIds().forEach(instance -> {
                parent.printfAnsi("@|bold,fg(magenta) /%d/%d : |@ @|bold,fg(green) %s |@ %n", objectModel.id, instance,
                        objectModel.name + " [Object v" + objectModel.version + "]");
                List<Integer> availableResources = objectEnabler.getAvailableResourceIds(instance);
                availableResources.forEach(resourceId -> {
                    ResourceModel resourceModel = objectModel.resources.get(resourceId);
                    parent.printfAnsi("  /%d : @|bold,fg(cyan) %s |@ %n", resourceId, resourceModel.name);
                });
            });
        }
    }

    /**
     * A command to create object enabler.
     */
    @Command(name = "create", description = "Enable a new Object", headerHeading = "%n", footer = "")
    static class CreateCommand implements Runnable {

        @Parameters(description = "Id of the LWM2M object to enable", index = "0")
        private Integer objectId;

        @Parameters(description = "Version of the LwM2M object to enable, if not precised the most recent one is picked",
                    index = "1",
                    arity = "0..1",
                    converter = VersionConverter.class)
        private Version version;

        @ParentCommand
        TBSectionCliInteractiveCommands parent;

        @Override
        public void run() {
            if (parent.client.getObjectTree().getObjectEnabler(objectId) != null) {
                parent.printf("Object %d already enabled.%n", objectId).flush();
            } else {
                ObjectModel objectModel;
                if (version != null)
                    objectModel = parent.repository.getObjectModel(objectId, version);
                else {
                    objectModel = parent.repository.getObjectModel(objectId);
                }
                if (objectModel == null) {
                    if (version == null) {
                        parent.printf("Unable to enable Object %d : there no model for this object.%n", objectId);
                    } else {
                        parent.printf("Unable to enable Object %d : there no model for this object in version %s.%n",
                                objectId, version);
                    }
                    parent.flush();
                } else {
                    ObjectsInitializer objectsInitializer = new ObjectsInitializer(new StaticModel(objectModel));
                    objectsInitializer.setDummyInstancesForObject(objectId);
                    LwM2mObjectEnabler object = objectsInitializer.create(objectId);
                    parent.client.getObjectTree().addObjectEnabler(object);
                }
            }
        }
    }

    /**
     * A command to delete object enabler.
     */
    @Command(name = "delete", description = "Disable a Object", headerHeading = "%n", footer = "")
    static class DeleteCommand implements Runnable {

        @Parameters(description = "Id of the LWM2M object to enable")
        private Integer objectId;

        @ParentCommand
        TBSectionCliInteractiveCommands parent;

        @Override
        public void run() {
            if (objectId == 0 || objectId == 1 || objectId == 3) {
                parent.printf("Object %d can not be disabled.%n", objectId).flush();
            } else if (parent.client.getObjectTree().getObjectEnabler(objectId) == null) {
                parent.printf("Object %d is not enabled.%n", objectId).flush();
            } else {
                parent.client.getObjectTree().removeObjectEnabler(objectId);
            }
        }
    }

    /**
     * A command to send an updateRegistration request.
     */
    @Command(name = "updateRegistration", description = "Trigger a registration update.", headerHeading = "%n", footer = "")
    static class UpdateRegistrationCommand implements Runnable {

        @ParentCommand
        TBSectionCliInteractiveCommands parent;

        @Override
        public void run() {
            parent.client.triggerRegistrationUpdate();
        }
    }

    /**
     * A command to restart client.
     */
    @Command(name = "reboot",
            description = "Restart client without update object.",
            headerHeading = "%n",
            footer = "",
            sortOptions = false)
    static class RebootCommand implements Runnable {

        @ParentCommand
        TBSectionCliInteractiveCommands parent;
        @Override
        public void run() {
            LwM2mObjectEnabler objectEnabler = parent.client.getObjectTree().getObjectEnabler(LwM2mId.DEVICE);
            if (objectEnabler != null && objectEnabler instanceof ObjectEnabler) {
                LwM2mInstanceEnabler instance = ((ObjectEnabler) objectEnabler).getInstance(0);
                if (instance instanceof MyDevice) {
                    MyDevice device = (MyDevice) instance;
                    device.triggerRebootClient();
                }
            }
        }
    }

    /**
     * A command to send data.
     */
    @Command(name = "send",
             description = "Send data to server: 'send current-value' or 'send collected-value'",
             subcommands = { SendCurrentValue.class, SendCollectedValue.class },
             synopsisSubcommandLabel = "(current-value | collected-value)",
             headerHeading = "%n",
             footer = "")
    static class SendCommand implements Runnable {

        @Option(names = { "-c", "--content-format" },
                defaultValue = "SENML_CBOR",
                description = { //
                        "Name (e.g. SENML_JSON) or code (e.g. 110) of Content Format used to send data.", //
                        "Default : ${DEFAULT-VALUE}" },
                converter = SendTBContentFormatConverver.class)
        ContentFormat contentFormat;

        public static class SendTBContentFormatConverver extends TBContentFormatConverter {
            public SendTBContentFormatConverver() {
                super(ContentFormat.SENML_CBOR, ContentFormat.SENML_JSON);
            }
        }

        long timeout = 2000; // ms

        @Spec
        CommandSpec spec;

        @ParentCommand
        TBSectionCliInteractiveCommands parent;

        @Override
        public void run() {
            throw new ParameterException(spec.commandLine(), "Missing required subcommand");
        }
    }

    @Command(name = "current-value", description = "Send current value", headerHeading = "%n", footer = "")
    static class SendCurrentValue implements Runnable {

        @Parameters(description = "Paths of data to send.", converter = StringLwM2mPathConverter.class, arity = "1..*")
        private List<String> paths;

        @ParentCommand
        SendCommand sendCommand;

        @Override
        public void run() {
            Map<String, LwM2mServer> registeredServers = sendCommand.parent.client.getRegisteredServers();
            if (registeredServers.isEmpty()) {
                sendCommand.parent.printf("There is no registered server to send to.%n").flush();
            }
            for (final LwM2mServer server : registeredServers.values()) {
                log.info("Sending Data to {} using {}.", server, sendCommand.contentFormat);
                ResponseCallback<SendResponse> responseCallback = (response) -> {
                    if (response.isSuccess())
                        log.info("Data sent successfully to {} [{}].", server, response.getCode());
                    else
                        log.info("Send data to {} failed [{}] : {}.", server, response.getCode(),
                                response.getErrorMessage() == null ? "" : response.getErrorMessage());
                };
                ErrorCallback errorCallback = (e) -> log.warn("Unable to send data to {}.", server, e);
                sendCommand.parent.client.getSendService().sendData(server, sendCommand.contentFormat, paths,
                        sendCommand.timeout, responseCallback, errorCallback);
            }
        }
    }

    @Command(name = "collected-value",  description = "Send values collected with 'send' command", headerHeading = "%n", footer = "")
    static class SendCollectedValue implements Runnable {
        @Parameters(description = "Paths of data to send.", converter = StringLwM2mPathConverter.class, arity = "1..*")
        private List<String> paths;

        @ParentCommand
        SendCommand sendCommand;

        @Override
        public void run() {
            // get registered servers
            Map<String, LwM2mServer> registeredServers = sendCommand.parent.client.getRegisteredServers();
            if (registeredServers.isEmpty()) {
                sendCommand.parent.printf("There is no registered server to send to.%n").flush();
            }
            // for each server send data
            for (final LwM2mServer server : registeredServers.values()) {
                log.info("Sending Collected data to {} using {}.", server, sendCommand.contentFormat);
                // send collected data
                SendService sendService = sendCommand.parent.client.getSendService();
                try {
                    sendService.getDataSender(ManualDataSender.DEFAULT_NAME, ManualDataSender.class)
                            .sendCollectedData(server, sendCommand.contentFormat, sendCommand.timeout, false);
                } catch (NoDataException e) {
                    sendCommand.parent.printf("No data collected, use `collect` command before.%n").flush();
                }
            }
        }
    }

    /**
     * A command to collect data.
     */
    @Command(name = "collect",
             description = "Collect data to send it later with 'send' command",
             headerHeading = "%n",
             footer = "")
    static class CollectCommand implements Runnable {

        @Parameters(description = "Paths of data to collect.", converter = LwM2mPathConverter.class)
        private List<LwM2mPath> paths;

        @ParentCommand
        TBSectionCliInteractiveCommands parent;

        @Override
        public void run() {
            parent.client.getSendService().getDataSender(ManualDataSender.DEFAULT_NAME, ManualDataSender.class)
                    .collectData(paths);
        }
    }

    /**
     * A command to move client.
     */
    @Command(name = "move",
             description = "Simulate client mouvement.",
             headerHeading = "%n",
             footer = "",
             sortOptions = false)
    static class MoveCommand implements Runnable {

        @ParentCommand
        TBSectionCliInteractiveCommands parent;

        @Option(names = { "-d", "north" }, description = "Move to the North")
        boolean north;

        @Option(names = { "-e", "east" }, description = "Move to the East")
        boolean east;

        @Option(names = { "-s", "south" }, description = "Move to the South")
        boolean south;

        @Option(names = { "-w", "west" }, description = "Move to the West")
        boolean west;

        @Override
        public void run() {
            LwM2mObjectEnabler objectEnabler = parent.client.getObjectTree().getObjectEnabler(LwM2mId.LOCATION);
            if (objectEnabler != null && objectEnabler instanceof ObjectEnabler) {
                LwM2mInstanceEnabler instance = ((ObjectEnabler) objectEnabler).getInstance(0);
                if (instance instanceof MyLocation) {
                    MyLocation location = (MyLocation) instance;
                    if (north)
                        location.moveLocation("d");
                    if (east)
                        location.moveLocation("e");
                    if (south)
                        location.moveLocation("s");
                    if (west)
                        location.moveLocation("w");
                }
            }
        }
    }

    @Command(name = "stop",
            description = "Stop the CLI",
            headerHeading = "%n",
            footer = "",
            sortOptions = false)
    static class ExitCommand implements Runnable {

        @ParentCommand
        TBSectionCliInteractiveCommands parent;
        @Override
        public void run() {
            System.out.println("Exiting...");
            parent.setRunning(false);
        }
    }

    @Command(
            name = "version",
            aliases = { "info", "about", "appInfo" },
            description = "Show application version and build information")
    static class VersionCommand implements Runnable {

        @ParentCommand
        TBSectionCliInteractiveCommands parent;

        @Override
        public void run() {
            String[] versions = parent.getAppVersion();
            parent.printfAnsi("@|bold,fg(yellow) Application Information : |@%n");
            for (String line : versions) {
                String[] l = line.split(" ");
                if (propertyLevelCLI_name.equals(l[0])){
                    parent.printfAnsi("@|bold,fg(magenta) -%s \t\t: |@ @|bold,fg(green) %s |@ %n", l[0], l[1]);
                } else {
                    parent.printfAnsi("@|bold,fg(magenta) -%s \t: |@ @|bold,fg(green) %s |@ %n", l[0], l[1]);
                }

            }
            parent.flush();

        }
    }

    // TODO "update"
    /*

    @Command(name = "update", description = "Update resource values on the client (example: update /5/0/1=pathForUrl)")
    static class UpdateResourceValues implements Runnable {

        @Parameters(description = "Paths with values to update, format: /objectId/instanceId/resourceId=value", arity = "1..*")
        private List<String> resourceUpdates;

        @ParentCommand
        TBSectionCliInteractiveCommands parent;

        @Override
        public void run() {
            Map<String, String> parsedUpdates = parsePaths(resourceUpdates);
            if (parsedUpdates.isEmpty()) {
                System.err.println("No valid updates.");
                return;
            }

            LwM2mObjectTree objectTree = parent.client.getObjectTree();
            List<String> success = new ArrayList<>();
            List<String> errors = new ArrayList<>();

            for (Map.Entry<String, String> entry : parsedUpdates.entrySet()) {
                String path = entry.getKey();
                String valueStr = entry.getValue();

                try {
                    String[] parts = path.replaceFirst("^/", "").split("/");
                    if (parts.length != 3) {
                        errors.add(path + " (invalid path format)");
                        continue;
                    }

                    int objectId = Integer.parseInt(parts[0]);
                    int instanceId = Integer.parseInt(parts[1]);
                    int resourceId = Integer.parseInt(parts[2]);

//                    LwM2mObjectEnabler object = objectTree.get(objectId);
//                    if (object == null) {
//                        errors.add(path + " (object not found)");
//                        continue;
//                    }
//
////                    WriteResponse response = object.write(LwM2mServer var1, WriteRequest var2);
//
//                    Map<Integer, LwM2mInstanceEnabler> instances = object.getAvailableInstanceIds();
//                    LwM2mInstanceEnabler instance = instances.get(instanceId);
//                    if (instance == null) {
//                        errors.add(path + " (instance not found)");
//                        continue;
//                    }

                    // Пробуємо виконати write
//                    if (supportsWrite(instance)) {
//                        Number numberValue = tryParseNumber(valueStr);
//                        LwM2mResource resource = LwM2mSingleResource.newFloatResource(resourceId, numberValue.floatValue());
//
//                        WriteResponse response = instance.write(
//                                null, // LwM2mServer identity, не потрібен тут
//                                false, // replace
//                                resourceId,
//                                resource
//                        );
//
//                        if (response.isSuccess()) {
//                            success.add(String.format("%s = %s (OK)", path, valueStr));
//                        } else {
//                            errors.add(String.format("%s = %s (write failed: %s)", path, valueStr, response.getCode()));
//                        }
//                    } else {
//                        errors.add(path + " (write() not supported by instance)");
//                    }
                } catch (Exception e) {
                    errors.add(path + " (exception: " + e.getMessage() + ")");
                }
            }

            success.forEach(s -> System.out.println("✅ " + s));
            errors.forEach(e -> System.err.println("❌ " + e));
        }

        private Map<String, String> parsePaths(List<String> entries) {
            Map<String, String> map = new LinkedHashMap<>();
            for (String entry : entries) {
                int eq = entry.indexOf('=');
                if (eq == -1 || eq == entry.length() - 1) continue;
                map.put(entry.substring(0, eq), entry.substring(eq + 1));
            }
            return map;
        }

        private boolean supportsWrite(LwM2mInstanceEnabler instance) {
            try {
                Method m = instance.getClass().getMethod("write", LwM2mServer.class, boolean.class, int.class, LwM2mResource.class);
                return m.getDeclaringClass() != LwM2mInstanceEnabler.class;
            } catch (NoSuchMethodException e) {
                return false;
            }
        }

        private Number tryParseNumber(String val) {
            try {
                return Integer.parseInt(val);
            } catch (NumberFormatException e) {
                try {
                    return Float.parseFloat(val);
                } catch (NumberFormatException ex) {
                    throw new IllegalArgumentException("Value is not a number: " + val);
                }
            }
        }
    }
    **/

}
