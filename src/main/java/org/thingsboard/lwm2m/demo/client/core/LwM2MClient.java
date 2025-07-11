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
package org.thingsboard.lwm2m.demo.client.core;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.californium.core.config.CoapConfig;
import org.eclipse.californium.elements.config.Configuration;
import org.eclipse.californium.scandium.config.DtlsConfig;
import org.eclipse.californium.scandium.config.DtlsConnectorConfig;
import org.eclipse.californium.scandium.dtls.MaxFragmentLengthExtension;
import org.eclipse.leshan.client.LeshanClient;
import org.eclipse.leshan.client.LeshanClientBuilder;
import org.eclipse.leshan.client.californium.endpoint.CaliforniumClientEndpointFactory;
import org.eclipse.leshan.client.californium.endpoint.CaliforniumClientEndpointsProvider;
import org.eclipse.leshan.client.californium.endpoint.ClientProtocolProvider;
import org.eclipse.leshan.client.californium.endpoint.coap.CoapOscoreProtocolProvider;
import org.eclipse.leshan.client.californium.endpoint.coaps.CoapsClientEndpointFactory;
import org.eclipse.leshan.client.californium.endpoint.coaps.CoapsClientProtocolProvider;
import org.eclipse.leshan.client.endpoint.LwM2mClientEndpointsProvider;
import org.eclipse.leshan.client.engine.DefaultRegistrationEngineFactory;
import org.eclipse.leshan.client.object.LwM2mTestObject;
import org.eclipse.leshan.client.object.Oscore;
import org.eclipse.leshan.client.object.Server;
import org.eclipse.leshan.client.resource.LwM2mObjectEnabler;
import org.eclipse.leshan.client.resource.ObjectsInitializer;
import org.eclipse.leshan.client.resource.listener.ObjectsListenerAdapter;
import org.eclipse.leshan.client.send.ManualDataSender;
import org.eclipse.leshan.client.servers.LwM2mServer;
import org.eclipse.leshan.core.californium.PrincipalMdcConnectionListener;
import org.eclipse.leshan.core.endpoint.Protocol;
import org.eclipse.leshan.core.model.LwM2mModelRepository;
import org.eclipse.leshan.core.node.LwM2mSingleResource;
import org.eclipse.leshan.core.node.codec.DefaultLwM2mDecoder;
import org.eclipse.leshan.core.node.codec.DefaultLwM2mEncoder;
import org.eclipse.leshan.core.node.codec.text.LwM2mNodeTextDecoder;
import org.eclipse.leshan.core.request.BindingMode;
import org.eclipse.leshan.core.request.BootstrapWriteRequest;
import org.eclipse.leshan.core.request.ContentFormat;
import org.eclipse.leshan.core.response.BootstrapWriteResponse;
import org.eclipse.leshan.transport.javacoap.client.coaptcp.endpoint.JavaCoapTcpClientEndpointsProvider;
import org.eclipse.leshan.transport.javacoap.client.coaptcp.endpoint.JavaCoapsTcpClientEndpointsProvider;
import org.eclipse.leshan.transport.javacoap.client.endpoint.JavaCoapClientEndpointsProvider;
import org.springframework.stereotype.Component;
import org.thingsboard.lwm2m.demo.client.DtlsSessionLogger;
import org.thingsboard.lwm2m.demo.client.cli.TBSectionsCliMain;
import org.thingsboard.lwm2m.demo.client.engine.DefaultClientEndpointNameProvider;
import org.thingsboard.lwm2m.demo.client.objects.FwLwM2MDevice;
import org.thingsboard.lwm2m.demo.client.objects.LwM2mBinaryAppDataContainer;
import org.thingsboard.lwm2m.demo.client.objects.MyDevice;
import org.thingsboard.lwm2m.demo.client.objects.MyLocation;
import org.thingsboard.lwm2m.demo.client.objects.RandomTemperatureSensor;
import org.thingsboard.lwm2m.demo.client.objects.SwLwM2MDevice;
import org.thingsboard.lwm2m.demo.client.util.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.eclipse.californium.core.config.CoapConfig.BLOCKWISE_ENTITY_TOO_LARGE_AUTO_FAILOVER;
import static org.eclipse.californium.core.config.CoapConfig.BLOCKWISE_STATUS_LIFETIME;
import static org.eclipse.californium.core.config.CoapConfig.BLOCKWISE_STRICT_BLOCK2_OPTION;
import static org.eclipse.californium.core.config.CoapConfig.DEFAULT_BLOCKWISE_STATUS_LIFETIME_IN_SECONDS;
import static org.eclipse.californium.core.config.CoapConfig.MAX_MESSAGE_SIZE;
import static org.eclipse.californium.core.config.CoapConfig.MAX_RESOURCE_BODY_SIZE;
import static org.eclipse.californium.core.config.CoapConfig.MAX_RETRANSMIT;
import static org.eclipse.californium.core.config.CoapConfig.PREFERRED_BLOCK_SIZE;
import static org.eclipse.californium.core.config.CoapConfig.RESPONSE_MATCHING;
import static org.eclipse.californium.scandium.config.DtlsConfig.DTLS_MAX_FRAGMENT_LENGTH;
import static org.eclipse.californium.scandium.config.DtlsConfig.DTLS_MAX_TRANSMISSION_UNIT;
import static org.eclipse.leshan.client.object.Security.noSec;
import static org.eclipse.leshan.client.object.Security.noSecBootstrap;
import static org.eclipse.leshan.client.object.Security.oscoreOnly;
import static org.eclipse.leshan.client.object.Security.oscoreOnlyBootstrap;
import static org.eclipse.leshan.client.object.Security.psk;
import static org.eclipse.leshan.client.object.Security.pskBootstrap;
import static org.eclipse.leshan.client.object.Security.rpk;
import static org.eclipse.leshan.client.object.Security.rpkBootstrap;
import static org.eclipse.leshan.client.object.Security.x509;
import static org.eclipse.leshan.client.object.Security.x509Bootstrap;
import static org.eclipse.leshan.core.LwM2mId.DEVICE;
import static org.eclipse.leshan.core.LwM2mId.FIRMWARE;
import static org.eclipse.leshan.core.LwM2mId.LOCATION;
import static org.eclipse.leshan.core.LwM2mId.OSCORE;
import static org.eclipse.leshan.core.LwM2mId.SECURITY;
import static org.eclipse.leshan.core.LwM2mId.SERVER;
import static org.eclipse.leshan.core.LwM2mId.SOFTWARE_MANAGEMENT;
import static org.thingsboard.lwm2m.demo.client.util.Utils.BINARY_APP_DATA_CONTAINER;
import static org.thingsboard.lwm2m.demo.client.util.Utils.CF_CONFIGURATION_FILENAME;
import static org.thingsboard.lwm2m.demo.client.util.Utils.CF_CONFIGURATION_HEADER;
import static org.thingsboard.lwm2m.demo.client.util.Utils.OBJECT_ID_LWM2M_TEST_OBJECT;
import static org.thingsboard.lwm2m.demo.client.util.Utils.OBJECT_ID_TEMPERATURE_SENSOR;
import static org.thingsboard.lwm2m.demo.client.util.Utils.fromLength;

@Slf4j
@Component
public class LwM2MClient {

    public LeshanClient create(TBSectionsCliMain cli, LwM2mModelRepository repository) throws Exception {
                // create Thingsboard Lwm2m Demo Client from command line option
        // Initialize object list
        final ObjectsInitializer initializer = new ObjectsInitializer(repository.getLwM2mModel());
        // handle OSCORE
        Integer oscoreObjectInstanceId;
        if (cli.oscore != null) {
            oscoreObjectInstanceId = 12345;
            Oscore oscoreObject = new Oscore(oscoreObjectInstanceId, cli.oscore.getOscoreSetting());
            initializer.setInstancesForObject(OSCORE, oscoreObject);
        } else {
            oscoreObjectInstanceId = null;
            initializer.setClassForObject(OSCORE, Oscore.class);
        }
        if (cli.main.bootstrap) {
            if (cli.identity.isPSK()) {
                // TODO OSCORE support OSCORE with DTLS/PSK
                initializer.setInstancesForObject(SECURITY, pskBootstrap(cli.main.url,
                        cli.identity.getPsk().identity.getBytes(), cli.identity.getPsk().sharekey.getBytes()));
                initializer.setClassForObject(SERVER, Server.class);
            } else if (cli.identity.isRPK()) {
                // TODO OSCORE support OSCORE with DTLS/RPK
                initializer.setInstancesForObject(SECURITY,
                        rpkBootstrap(cli.main.url, cli.identity.getRPK().cpubk.getEncoded(),
                                cli.identity.getRPK().cprik.getEncoded(), cli.identity.getRPK().spubk.getEncoded()));
                initializer.setClassForObject(SERVER, Server.class);
            } else if (cli.identity.isx509()) {
                // TODO OSCORE support OSCORE with DTLS/X509
                initializer.setInstancesForObject(SECURITY,
                        x509Bootstrap(cli.main.url, cli.identity.getX509().ccert.getEncoded(),
                                cli.identity.getX509().cprik.getEncoded(), cli.identity.getX509().scert.getEncoded(),
                                cli.identity.getX509().certUsage.code));
                initializer.setClassForObject(SERVER, Server.class);
            } else {
                if (oscoreObjectInstanceId != null) {
                    initializer.setInstancesForObject(SECURITY,
                            oscoreOnlyBootstrap(cli.main.url, oscoreObjectInstanceId));
                } else {
                    initializer.setInstancesForObject(SECURITY, noSecBootstrap(cli.main.url));
                }
                initializer.setClassForObject(SERVER, Server.class);
            }
        } else {
            BindingMode serverBindingMode = BindingMode.fromProtocol(Protocol.fromUri(cli.main.url));

            if (cli.identity.isPSK()) {
                // TODO OSCORE support OSCORE with DTLS/PSK
                initializer.setInstancesForObject(SECURITY, psk(cli.main.url, 123,
                        cli.identity.getPsk().identity.getBytes(), cli.identity.getPsk().sharekey.getBytes()));
                initializer.setInstancesForObject(SERVER, new Server(123, cli.main.lifetimeInSec,
                        EnumSet.of(serverBindingMode), false, serverBindingMode));
            } else if (cli.identity.isRPK()) {
                // TODO OSCORE support OSCORE with DTLS/RPK
                initializer.setInstancesForObject(SECURITY,
                        rpk(cli.main.url, 123, cli.identity.getRPK().cpubk.getEncoded(),
                                cli.identity.getRPK().cprik.getEncoded(), cli.identity.getRPK().spubk.getEncoded()));
                initializer.setInstancesForObject(SERVER, new Server(123, cli.main.lifetimeInSec,
                        EnumSet.of(serverBindingMode), false, serverBindingMode));
            } else if (cli.identity.isx509()) {
                // TODO OSCORE support OSCORE with DTLS/X509
                initializer.setInstancesForObject(SECURITY,
                        x509(cli.main.url, 123, cli.identity.getX509().ccert.getEncoded(),
                                cli.identity.getX509().cprik.getEncoded(), cli.identity.getX509().scert.getEncoded(),
                                cli.identity.getX509().certUsage.code));
                initializer.setInstancesForObject(SERVER, new Server(123, cli.main.lifetimeInSec,
                        EnumSet.of(serverBindingMode), false, serverBindingMode));
            } else {
                if (oscoreObjectInstanceId != null) {
                    initializer.setInstancesForObject(SECURITY, oscoreOnly(cli.main.url, 123, oscoreObjectInstanceId));
                } else {
                    initializer.setInstancesForObject(SECURITY, noSec(cli.main.url, 123));
                }
                initializer.setInstancesForObject(SERVER, new Server(123, cli.main.lifetimeInSec,
                        EnumSet.of(serverBindingMode), false, serverBindingMode));
            }
        }
        if (cli.main.testObject && cli.main.testOta){
            throw new IllegalStateException("Only one of these parameters (`-tobj` or `-tota`) can be used at a time.");
        }
        Utils.setOtaFolder(cli.main.otaFolder);
        initializer.setInstancesForObject(DEVICE, new MyDevice(cli.main.timeDataFrequency));
        initializer.setInstancesForObject(FIRMWARE, new FwLwM2MDevice(cli.main.timeDataFrequency, cli.main.testObject, cli.main.testOta));

        initializer.setInstancesForObject(SOFTWARE_MANAGEMENT, new SwLwM2MDevice(cli.main.timeDataFrequency, cli.main.testObject, cli.main.testOta));
        initializer.setInstancesForObject(LOCATION, new MyLocation(cli.main.timeDataFrequency, cli.location.position.latitude,
                cli.location.position.longitude, cli.location.scaleFactor));
        initializer.setInstancesForObject(BINARY_APP_DATA_CONTAINER, new LwM2mBinaryAppDataContainer(cli.main.timeDataFrequency, 0),
                new LwM2mBinaryAppDataContainer(cli.main.timeDataFrequency, 1));
        initializer.setInstancesForObject(OBJECT_ID_TEMPERATURE_SENSOR, new RandomTemperatureSensor(cli.main.timeDataFrequency));
        initializer.setInstancesForObject(OBJECT_ID_LWM2M_TEST_OBJECT, new LwM2mTestObject());

        List<LwM2mObjectEnabler> enablers = initializer.createAll();

        // Configure Registration Engine
        DefaultRegistrationEngineFactory engineFactory = new DefaultRegistrationEngineFactory();
        if (cli.main.comPeriodInSec != null)
            engineFactory.setCommunicationPeriod(cli.main.comPeriodInSec * 1000);
        engineFactory.setReconnectOnUpdate(cli.dtls.reconnectOnUpdate);
        engineFactory.setResumeOnConnect(!cli.dtls.forceFullhandshake);
        engineFactory.setQueueMode(cli.main.queueMode);

        // Create Californium Endpoints Provider:
        // --------------------------------------
        // Define Custom CoAPS protocol provider
        CoapsClientProtocolProvider customCoapsProtocolProvider = new CoapsClientProtocolProvider() {
            @Override
            public CaliforniumClientEndpointFactory createDefaultEndpointFactory() {
                return new CoapsClientEndpointFactory() {

                    @Override
                    protected DtlsConnectorConfig.Builder createRootDtlsConnectorConfigBuilder(
                            Configuration configuration) {
                        DtlsConnectorConfig.Builder builder = super.createRootDtlsConnectorConfigBuilder(configuration);

                        // Add DTLS Session lifecycle logger
                        builder.setSessionListener(new DtlsSessionLogger());

                        // Add MDC for connection logs
                        if (cli.helpsOptions.getVerboseLevel() > 0) {
                            builder.setConnectionListener(new PrincipalMdcConnectionListener());
                        }
                        MaxFragmentLengthExtension.Length length = fromLength(1024);
                        builder.set(DTLS_MAX_FRAGMENT_LENGTH, length);
                        builder.set(DTLS_MAX_TRANSMISSION_UNIT, 1024);
                        return builder;
                    };
                };
            }
        };

        // Create client protocol Provider
        List<ClientProtocolProvider> protocolProvider = new ArrayList<>();
        if (!cli.main.useJavaCoap) {
            protocolProvider.add(new CoapOscoreProtocolProvider());
        }
        protocolProvider.add(customCoapsProtocolProvider);
        CaliforniumClientEndpointsProvider.Builder endpointsBuilder = new CaliforniumClientEndpointsProvider.Builder(
                protocolProvider.toArray(new ClientProtocolProvider[protocolProvider.size()]));

        // Create Californium Configuration
        Configuration clientCoapConfig = endpointsBuilder.createDefaultConfiguration();
        // Set some DTLS stuff
        // These configuration values are always overwritten by CLI therefore set them to transient.

        // Persist configuration
        File configFile = new File(CF_CONFIGURATION_FILENAME);
        if (configFile.isFile()) {
            clientCoapConfig.load(configFile);
        } else {
            clientCoapConfig.store(configFile, CF_CONFIGURATION_HEADER);
        }

        // custom for ota
        if (cli.main.testOta || cli.main.testObject) {
            clientCoapConfig.setTransient(DtlsConfig.DTLS_RECOMMENDED_CIPHER_SUITES_ONLY);
            clientCoapConfig.setTransient(DtlsConfig.DTLS_CONNECTION_ID_LENGTH);
            clientCoapConfig.set(DtlsConfig.DTLS_RECOMMENDED_CIPHER_SUITES_ONLY, !cli.dtls.supportDeprecatedCiphers);
            clientCoapConfig.set(DtlsConfig.DTLS_CONNECTION_ID_LENGTH, cli.dtls.cid);
            clientCoapConfig.set(BLOCKWISE_STRICT_BLOCK2_OPTION, true);
            clientCoapConfig.set(BLOCKWISE_ENTITY_TOO_LARGE_AUTO_FAILOVER, true);
            clientCoapConfig.set(BLOCKWISE_STATUS_LIFETIME, DEFAULT_BLOCKWISE_STATUS_LIFETIME_IN_SECONDS, TimeUnit.SECONDS);
            clientCoapConfig.set(MAX_RESOURCE_BODY_SIZE, 256 * 1024 * 1024);
            clientCoapConfig.set(RESPONSE_MATCHING, CoapConfig.MatcherMode.RELAXED);
            clientCoapConfig.set(PREFERRED_BLOCK_SIZE, 1024);
            clientCoapConfig.set(MAX_MESSAGE_SIZE, 1024);
            clientCoapConfig.set(MAX_RETRANSMIT, 4);
        }

        if (cli.dtls.ciphers != null) {
            clientCoapConfig.set(DtlsConfig.DTLS_CIPHER_SUITES, cli.dtls.ciphers);
        }

        // Set Californium Configuration

        endpointsBuilder.setConfiguration(clientCoapConfig);
        endpointsBuilder.setClientAddress(cli.main.localAddress);

        // creates EndpointsProvider
        List<LwM2mClientEndpointsProvider> endpointsProvider = new ArrayList<>();
        endpointsProvider.add(endpointsBuilder.build());
        if (cli.main.useJavaCoap) {
            endpointsProvider.add(new JavaCoapClientEndpointsProvider());
        }
        endpointsProvider.add(new JavaCoapTcpClientEndpointsProvider());
        endpointsProvider.add(new JavaCoapsTcpClientEndpointsProvider());

        // Create client
        LeshanClientBuilder builder = new LeshanClientBuilder(
                new DefaultClientEndpointNameProvider(cli.main.endpoint, cli.main.endpointNameMode).getEndpointName());
        builder.setObjects(enablers);
        builder.setEndpointsProviders(
                endpointsProvider.toArray(new LwM2mClientEndpointsProvider[endpointsProvider.size()]));
        builder.setDataSenders(new ManualDataSender());
        if (cli.identity.isx509())
            builder.setTrustStore(cli.identity.getX509().trustStore);
        builder.setRegistrationEngineFactory(engineFactory);
        if (cli.main.supportOldFormat) {
            builder.setDecoder(new DefaultLwM2mDecoder(true));
            builder.setEncoder(new DefaultLwM2mEncoder(true));
        }
        builder.setAdditionalAttributes(cli.main.additionalAttributes);
        builder.setBootstrapAdditionalAttributes(cli.main.bsAdditionalAttributes);
        final LeshanClient client = builder.build();

        // Handle Factory Bootstrap option
        if (cli.main.factoryBootstrap != null) {
            LwM2mNodeTextDecoder textDecoder = new LwM2mNodeTextDecoder();
            cli.main.factoryBootstrap.forEach((resourcePath, resourceValue) -> {
                BootstrapWriteResponse response = null;
                try {
                    // get resource from string resource value
                    LwM2mSingleResource resource = textDecoder.decode(resourceValue.getBytes(), resourcePath,
                            repository.getLwM2mModel(), LwM2mSingleResource.class);
                    // try to write this resource
                    response = client.getObjectTree().getObjectEnabler(resourcePath.getObjectId()).write(
                            LwM2mServer.SYSTEM, new BootstrapWriteRequest(resourcePath, resource, ContentFormat.TEXT));
                } catch (RuntimeException e) {
                    // catch any error
                    throw new IllegalStateException(
                            String.format(" --factory-bootstrap failed : unable to write resource %s", resourcePath),
                            e);
                }
                // Raise error if bootstrap write request failed
                if (response == null || !response.isSuccess()) {
                    throw new IllegalStateException(
                            String.format("--factory-bootstrap failed : unable to write resource %s%s", resourcePath,
                                    response.getErrorMessage() == null ? "" : " : " + response.getErrorMessage()));
                }
            });
        }

        // Add some log about object tree life cycle.
        client.getObjectTree().addListener(new ObjectsListenerAdapter() {

            @Override
            public void objectRemoved(LwM2mObjectEnabler object) {
                log.info("Object {} v{} disabled.", object.getId(), object.getObjectModel().version);
            }

            @Override
            public void objectAdded(LwM2mObjectEnabler object) {
                log.info("Object {} v{} enabled.", object.getId(), object.getObjectModel().version);
            }
        });

        return client;
    }
}