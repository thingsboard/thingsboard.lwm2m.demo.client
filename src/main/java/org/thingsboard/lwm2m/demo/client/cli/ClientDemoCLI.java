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

import org.eclipse.californium.scandium.dtls.cipher.CipherSuite;
import org.eclipse.leshan.core.CertificateUsage;
import org.eclipse.leshan.core.demo.cli.MultiParameterException;
import org.eclipse.leshan.core.demo.cli.converters.CIDConverter;
import org.eclipse.leshan.core.demo.cli.converters.InetAddressConverter;
import org.eclipse.leshan.core.demo.cli.converters.ResourcePathConverter;
import org.eclipse.leshan.core.demo.cli.converters.StrictlyPositiveIntegerConverter;
import org.eclipse.leshan.core.endpoint.Protocol;
import org.eclipse.leshan.core.node.LwM2mPath;
import org.eclipse.leshan.core.util.StringUtils;
import org.thingsboard.lwm2m.demo.client.VersionProvider;
import org.thingsboard.lwm2m.demo.client.engine.DefaultClientEndpointNameProvider.Mode;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.ITypeConverter;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Spec;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This is the class defining the Command Line Interface of Thingsboard Lwm2m Client Demo.
 */
@Command(name = "thingsboard-lw-demo-client",
         sortOptions = false,
         description = "%n"//
                 + "@|italic " //
                 + "This is Thingsboard Lwm2m Demo Client implemented with Leshan library.%n" //
                 + "You can launch it without any option and it will try to register to a LWM2M server at " + "coap://"
                 + ClientDemoCLI.DEFAULT_COAP_URL + ".%n" //
                 + "%n" //
                 + "Californium is used as CoAP library and some CoAP parameters can be tweaked in 'Californium.properties' file." //
                 + "|@%n%n",
         versionProvider = VersionProvider.class)
public class ClientDemoCLI implements Runnable {

//    public static final String DEFAULT_COAP_URL = "localhost:" + CoAP.DEFAULT_COAP_PORT;
    public static final String DEFAULT_COAP_URL = "localhost:" + 5685;
//    public static final String DEFAULT_COAPS_URL = "localhost:" + CoAP.DEFAULT_COAP_SECURE_PORT;
    public static final String DEFAULT_COAPS_URL = "localhost:" + 5686;

    private static String defaultEndpoint() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            return "ThingsboardLwm2mClientDemo";
        }
    }

    @Mixin
    public StandardHelpOptions helpsOptions;

    /* ********************************** General Section ******************************** */
    @ArgGroup(validate = false, heading = "%n")
    public GeneralSection main = new GeneralSection();

    public static class GeneralSection {

        @Option(names = { "-u", "--server-url" },
                description = { //
                        "Set the server URL. If port is missing it will be added automatically with default value.", //
                        "Default: ", //
                        "  - " + DEFAULT_COAP_URL + " for coap", //
                        "  - " + DEFAULT_COAPS_URL + " for coaps" })
        public String url;

        @Option(names = { "-b", "--bootstrap" },
                description = { "Do bootstrap instead of registration.",
                        "In this case your server-url should target a LWM2M bootstrap server instead of a LWM2M server." })
        public boolean bootstrap;

        @Option(names = { "-n", "--endpoint-name" },
                description = { //
                        "Set the endpoint name of the Client.", //
                        "Default the hostname or 'ThingsboardLwm2mClientDemo' if no hostname." })
        public String endpoint = ClientDemoCLI.defaultEndpoint();

        @Option(names = { "-l", "--lifetime" },
                defaultValue = "300" /* 5 minutes */,
                description = { //
                        "The registration lifetime in seconds.", //
                        "Ignored if -b is used.", //
                        "Default : ${DEFAULT-VALUE}s." },
                converter = StrictlyPositiveIntegerConverter.class)
        public Integer lifetimeInSec;

        @Option(names = { "-cp", "--communication-period" },
                description = { //
                        "The communication period in seconds", //
                        "It should be smaller than the lifetime.", //
                        "It will be used even if -b is used." },
                converter = StrictlyPositiveIntegerConverter.class)
        public Integer comPeriodInSec;

        @Option(names = { "-q", "--queue-mode" }, description = { "Client use queue mode (not fully implemented)." })
        public boolean queueMode;

        @Option(names = { "-lh", "--local-address" },
                description = { //
                        "Set the local CoAP address of the Client.", //
                        "Default: any local address." },
                converter = InetAddressConverter.class)
        public InetAddress localAddress;

        @Option(names = { "-m", "--models-folder" },
                description = { //
                        "A folder which contains object models in OMA DDF(xml)format.", //
                        "syntax is :", //
                        "-m ./", //
                        "-m ./models", //
                        "-m /absolute/path/to" })
        public File modelsFolder;

        @Option(names = { "-o", "--ota-folder" },
                description = { //
                        "Path to the folder containing OTA information for firmware or software.", //
                        "syntax is :", //
                        "-o ./", //
                        "-o ./ota", //
                        "-o /absolute/path/to" })
        public String otaFolder;

        @Option(names = { "-aa", "--additional-attributes" },
                description = { //
                        "Use additional attributes at registration time.", //
                        "syntax is :", //
                        "-aa attrName1=attrValue1,attrName2=\\\"attrValue2\\\"" },
                split = ",")

        public Map<String, String> additionalAttributes;

        @Option(names = { "-bsaa", "--bootstrap-additional-attributes" },
                description = { //
                        "Use additional attributes at bootstrap time.", //
                        "syntax is :", //
                        " -bsaa attrName1=attrValue1,attrName2=\\\"attrValue2\\\"" },
                split = ",")

        public Map<String, String> bsAdditionalAttributes;

        @Option(names = { "-ocf", "--support-old-format" },
                description = { //
                        "Activate support of old/unofficial content format.", //
                        "See https://github.com/eclipse/leshan/pull/720" })
        public boolean supportOldFormat;

        @Option(names = { "-jc", "--use-java-coap" },
                description = { //
                        "Use java-coap for CoAP protocol instead of Californium." })
        public boolean useJavaCoap;

        @Option(names = { "-fb", "--factory-bootstrap" },
                description = { //
                        "Can be used to initialize existing resource with custom value.", //
                        "LWM2M Text Content format encoding should be used for resource value", //
                        "E.g. to change Short Server ID :", //
                        " -fb /0/0/10=1234,/1/0/0=1234", //
                },
                split = ",",
                converter = ResourcePathConverter.class)

        public Map<LwM2mPath, String> factoryBootstrap;

        @Option(names = { "-nm", "--endpoint-name-mode" },
                description = { //
                        "Can be used to set if client should or should not send client endpoint name during registration or bootstrap.", //
                        "Default : ${DEFAULT-VALUE}.",//
                })
        public Mode endpointNameMode = Mode.ALWAYS;

        @Option(names = { "-tobj", "--test-objects" },
                description = { //
                        "Enables testing of custom-programmed algorithms (like OTA). ", //
                        "Test mode is available for Object IDs 5, 9.", //
                        "Syntax example:", //
                        "-tobj", //
                })
        public boolean testObject;

        @Option(names = { "-tota", "--test-ota" },
                description = { //
                        "Allows you to test firmware/software updates using real OTA files. object 19 (instance 65456 for firmware, 65457 for software) to pass additional ota file information in json format.", //
                        "Test mode supports object IDs 5 and 9, using object 19", //
                        "Object 19 (instance 65534 for firmware, 65535 for software) to pass additional ota file information in json format.", //
                        "Syntax example:", //
                        "-tota", //
                })
        public boolean testOta;
    }

    /* ********************************** Location Section ******************************** */
    @ArgGroup(validate = false,
              heading = "%n@|bold,underline Object Location Options|@ %n%n"//
                      + "@|italic " //
                      + "A very Simple implementation of Object (6) location with simulated values is provided. Those options aim to set this object." //
                      + "|@%n%n")
    public LocationSection location = new LocationSection();

    public static class LocationSection {
        @Option(names = { "-pos", "--initial-position" },
                defaultValue = "random",
                description = { //
                        "Set the initial location (latitude, longitude) of the device to be reported by the Location object.", //
                        "Format: lat_float:long_float" },
                converter = PositionConverter.class)
        public Position position;

        public static class Position {
            public Float latitude;
            public Float longitude;
        };

        private static class PositionConverter implements ITypeConverter<Position> {
            @Override
            public Position convert(String pos) {
                Position position = new Position();
                if (pos.equals("random"))
                    return position;

                int colon = pos.indexOf(':');
                if (colon == -1 || colon == 0 || colon == pos.length() - 1)
                    throw new IllegalArgumentException(
                            "Position must be a set of two floats separated by a colon, e.g. 48.131:11.459");
                position.latitude = Float.valueOf(pos.substring(0, colon));
                position.longitude = Float.valueOf(pos.substring(colon + 1));
                return position;
            }
        };

        @Option(names = { "-sf", "--scale-factor" },
                defaultValue = "1.0",
                description = { //
                        "Scale factor to apply when shifting position.", //
                        "Default is ${DEFAULT-VALUE}." })
        public Float scaleFactor;

    }

    /* ********************************** DTLS Section ******************************** */
    @ArgGroup(validate = false,
              heading = "%n@|bold,underline DTLS Options|@ %n%n"//
                      + "@|italic " //
                      + "Here some options aiming to configure the client behavior when it uses CoAP over DTLS." //
                      + "%n" //
                      + "Scandium is used as DTLS library and some DTLS parameters can be tweaked in 'Californium.properties' file." //
                      + "|@%n%n")
    public DTLSSection dtls = new DTLSSection();

    public static class DTLSSection {

        @Option(names = { "-r", "--rehanshake-on-update" },
                description = { //
                        "Force reconnection/rehandshake on registration update." })
        public boolean reconnectOnUpdate;

        @Option(names = { "-f", "--force-full-handshake" },
                description = { //
                        "By default client will try to resume DTLS session by using abbreviated Handshake. This option force to always do a full handshake." })
        public boolean forceFullhandshake;

        @Option(names = { "-cid", "--connection-id" },
                defaultValue = "off",
                description = { //
                        "Control usage of DTLS connection ID.", //
                        "- 'on' to activate Connection ID support (same as -cid 0)", //
                        "- 'off' to deactivate it", //
                        "- Positive value define the size in byte of CID generated.", //
                        "- 0 value means we accept to use CID but will not generated one for foreign peer.", //
                        "Default: off" },
                converter = ClientCIDConverter.class)
        public Integer cid;

        private static class ClientCIDConverter extends CIDConverter {
            public ClientCIDConverter() {
                super(0);
            }
        };

        @Option(names = { "-c", "--cipher-suites" }, //
                description = { //
                        "Define cipher suites to use.", //
                        "CipherCuite enum value separated by ',' without spaces.", //
                        "E.g: TLS_PSK_WITH_AES_128_CCM_8,TLS_PSK_WITH_AES_128_CCM " },
                split = ",")
        public List<CipherSuite> ciphers;

        @Option(names = { "-oc", "--support-deprecated-ciphers" },
                description = { //
                        "Activate support of old/deprecated cipher suites." })
        public boolean supportDeprecatedCiphers;
    }

    /* ********************************** Identity Section ******************************** */
    @ArgGroup(exclusive = true)
    public IdentitySection identity = new IdentitySection();

    /* ********************************** OSCORE Section ******************************** */
    @ArgGroup(exclusive = false,
              heading = "%n@|bold,underline OSCORE Options|@ %n%n"//
                      + "@|italic " //
                      + "By default Leshan demo does not use OSCORE.%n"//
                      + "|@" + "@|red, OSCORE implementation in Leshan is in an experimental state.|@%n" //
                      + "%n")

    public OscoreSection oscore;

    @Spec
    CommandSpec spec;

    @Override
    public void run() {
        // Some post-validation which imply several options.
        // For validation about only one option, just use ITypeConverter instead

        // check certificate usage
        if (identity.isx509()) {
            if (identity.getX509().certUsage == CertificateUsage.SERVICE_CERTIFICATE_CONSTRAINT
                    && identity.getX509().trustStore.isEmpty()) {
                throw new MultiParameterException(spec.commandLine(),
                        "You need to set a truststore when you are using \"service certificate constraint\" usage",
                        "-cu", "-ts");
            }
        }

        // check OSCORE
        if (oscore != null) {
            oscore.validateOscoreSetting(spec.commandLine());
        }

        normalizedServerUrl();

        // validate url.
        // extract scheme
        int indexOf = main.url.indexOf("://");
        String scheme = main.url.substring(0, indexOf);
        // check URI scheme is supported
        List<String> supportedUnsecuredProtocol = Arrays.asList(Protocol.COAP, Protocol.COAP_TCP) //
                .stream().map(Protocol::getUriScheme).collect(Collectors.toList());
        List<String> supportedTlsBasedProtocol = Arrays.asList(Protocol.COAPS, Protocol.COAPS_TCP) //
                .stream().map(Protocol::getUriScheme).collect(Collectors.toList());
        List<String> allSupportedProtocol = Stream
                .concat(supportedUnsecuredProtocol.stream(), supportedTlsBasedProtocol.stream())
                .collect(Collectors.toList());

        if (!allSupportedProtocol.contains(scheme)) {
            throw new MultiParameterException(spec.commandLine(),
                    String.format("Invalid URL %s : unknown scheme '%s', we support only %s for now", main.url, scheme,
                            String.join(" or ", allSupportedProtocol)),
                    "-u");
        }
        // check scheme matches configuration
        if (identity.hasIdentity()) {
            if (!supportedTlsBasedProtocol.contains(scheme)) {
                throw new MultiParameterException(spec.commandLine(), String.format(
                        "Invalid URL %s : '%s' scheme must be used without PSK, RPK or x509 option. Do you mean %s ? ",
                        main.url, scheme, String.join(" or ", supportedTlsBasedProtocol)), "-u");
            }
        } else {
            if (!supportedUnsecuredProtocol.contains(scheme)) {
                throw new MultiParameterException(spec.commandLine(), String.format(
                        "Invalid URL %s : '%s' scheme must be used with PSK, RPK or x509 option. Do you mean %s ? ",
                        main.url, scheme, String.join(" or ", supportedUnsecuredProtocol)), "-u");
            }
        }
    }

    protected void normalizedServerUrl() {
        String url = main.url;
        if (url == null)
            url = "localhost";

        // try to guess if port is present.
        String[] splittedUrl = url.split(":");
        String port = splittedUrl[splittedUrl.length - 1];
        if (!StringUtils.isNumeric(port)) {
            // it seems port is not present, so we try to add it
            if (identity.hasIdentity()) {
//                main.url = url + ":" + CoAP.DEFAULT_COAP_SECURE_PORT;
                main.url = url + ":" + 5686;
            } else {
//                main.url = url + ":" + CoAP.DEFAULT_COAP_PORT;
                main.url = url + ":" + 5685;
            }
        }

        // try to guess if scheme is present :
        if (!main.url.contains("://")) {
            // it seems scheme is not present try to add it
            if (identity.hasIdentity()) {
                main.url = "coaps://" + main.url;
            } else {
                main.url = "coap://" + main.url;
            }
        }
    }
}
