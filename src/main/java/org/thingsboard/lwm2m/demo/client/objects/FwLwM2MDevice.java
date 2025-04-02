/**
 * Copyright © 2016-2024 The Thingsboard Authors
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.thingsboard.lwm2m.demo.client.objects;


import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.core.config.CoapConfig;
import org.eclipse.californium.core.network.CoapEndpoint;
import org.eclipse.californium.elements.config.Configuration;
import org.eclipse.californium.elements.util.DaemonThreadFactory;
import org.eclipse.leshan.client.resource.BaseInstanceEnabler;
import org.eclipse.leshan.client.servers.LwM2mServer;
import org.eclipse.leshan.core.model.ObjectModel;
import org.eclipse.leshan.core.node.LwM2mResource;
import org.eclipse.leshan.core.request.argument.Arguments;
import org.eclipse.leshan.core.response.ExecuteResponse;
import org.eclipse.leshan.core.response.ReadResponse;
import org.eclipse.leshan.core.response.WriteResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.Destroyable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.eclipse.californium.core.config.CoapConfig.DEFAULT_BLOCKWISE_STATUS_LIFETIME_IN_SECONDS;

public class FwLwM2MDevice extends BaseInstanceEnabler implements Destroyable {

    private static final Logger LOG = LoggerFactory.getLogger(MyDevice.class);
    private static final List<Integer> supportedResources = Arrays.asList(0, 1, 2, 3, 5, 6, 7, 9);

    private final ScheduledExecutorService scheduler = new ScheduledThreadPoolExecutor(1,
            new DaemonThreadFactory(getClass().getSimpleName() + "-test-scope"));
    private final AtomicInteger state = new AtomicInteger(0);

    private final AtomicInteger updateResult = new AtomicInteger(0);
    private final Timer timer;
    private static final String OTA_FOLDER_DEF = "./ota";
    private static final String OTA_FILE_NANE_DEF = "otaPackageFW.bin";
    private boolean testObject;
    private boolean testOta;
    private String packageURI;
    private String fileNameFW;
    private String otaFolder;

    public FwLwM2MDevice() {
        // notify new date each 5 second
        this.timer = new Timer("5 - Device-Current Time");
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                fireResourceChange(1);
                fireResourceChange(9);
            }
        }, 5000, 5000);
    }

    public FwLwM2MDevice(boolean testObject, boolean testOta, String otaFolder) {
        this.testObject = testObject;
        this.testOta = testOta;
        this.fileNameFW = OTA_FILE_NANE_DEF;
        this.otaFolder = otaFolder != null ? otaFolder : OTA_FOLDER_DEF;
        // notify new date each 5 second
        this.timer = new Timer("5 - Device-Current Time");
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                fireResourceChange(1);
                fireResourceChange(9);
            }
        }, 5000, 5000);
    }


    @Override
    public ReadResponse read(LwM2mServer identity, int resourceId) {
        if (!identity.isSystem())
            LOG.info("Read on Device resource /{}/{}/{}", getModel().id, getId(), resourceId);
        switch (resourceId) {
            case 1:
                return ReadResponse.success(resourceId, getPackageURI());
            case 3:
                return ReadResponse.success(resourceId, getState());
            case 5:
                return ReadResponse.success(resourceId, getUpdateResult());
            case 6:
                return ReadResponse.success(resourceId, getPkgName());
            case 7:
                return ReadResponse.success(resourceId, getPkgVersion());
            case 9:
                return ReadResponse.success(resourceId, getFirmwareUpdateDeliveryMethod());
            default:
                return super.read(identity, resourceId);
        }
    }

    @Override
    public ExecuteResponse execute(LwM2mServer identity, int resourceId, Arguments arguments) {
        String withArguments = "";
        if (!arguments.isEmpty())
            withArguments = " with arguments " + arguments;
        LOG.info("Execute on Device resource /{}/{}/{} {}", getModel().id, getId(), resourceId, withArguments);

        switch (resourceId) {
            case 2:
                if (this.testObject) {
                    startUpdating();
                }
                return ExecuteResponse.success();
            default:
                return super.execute(identity, resourceId, arguments);
        }
    }

    @Override
    public WriteResponse write(LwM2mServer identity, boolean replace, int resourceId, LwM2mResource value) {
        LOG.info("Write on Device resource /{}/{}/{}", getModel().id, getId(), resourceId);

        switch (resourceId) {
            case 0:
                savePayloadToFile((byte[]) value.getValue(), getPathOtaFW());
                return WriteResponse.success();
            case 1:
                if (this.testObject) {
                    startDownloading();
                } else if (this.testOta) {
                    setPackageURI((String) value.getValue());
                    fireResourceChange(resourceId);
                }
                return WriteResponse.success();
            default:
                return super.write(identity, replace, resourceId, value);
        }
    }

    private String getPackageURI() {
        return packageURI;
    }

    private void setPackageURI(String packageURI) {
        this.packageURI = packageURI;
    }

    private int getState() {
        return state.get();
    }

    private int getUpdateResult() {
        return updateResult.get();
    }

    private String getPkgName() {
        return "firmware";
    }

    private String getPkgVersion() {
        return "1.0.0";
    }

    private int getFirmwareUpdateDeliveryMethod() {
        return 2;
    }

    @Override
    public List<Integer> getAvailableResourceIds(ObjectModel model) {
        return supportedResources;
    }

    @Override
    public void destroy() {
        timer.cancel();
    }

    private void startDownloading() {
        scheduler.schedule(() -> {
            try {
                state.set(1);
                fireResourceChange(3);
                Thread.sleep(100);
                state.set(2);
                fireResourceChange(3);
            } catch (Exception e) {
            }
        }, 100, TimeUnit.MILLISECONDS);
    }

    private void startUpdating() {
        scheduler.schedule(() -> {
            try {
                state.set(3);
                fireResourceChange(3);
                Thread.sleep(100);
                updateResult.set(1);
                fireResourceChange(5);
            } catch (Exception e) {
            }
        }, 100, TimeUnit.MILLISECONDS);
    }

    private void appendFwDataFromUri() {
        CoapClient client = new CoapClient(getPackageURI());
        Configuration networkConfig = new Configuration();
        networkConfig.set(CoapConfig.BLOCKWISE_STRICT_BLOCK2_OPTION, true);
        networkConfig.set(CoapConfig.BLOCKWISE_ENTITY_TOO_LARGE_AUTO_FAILOVER, true);
        networkConfig.set(CoapConfig.BLOCKWISE_STATUS_LIFETIME, DEFAULT_BLOCKWISE_STATUS_LIFETIME_IN_SECONDS, TimeUnit.SECONDS);
        networkConfig.set(CoapConfig.MAX_RESOURCE_BODY_SIZE, 256 * 1024 * 1024);
        networkConfig.set(CoapConfig.RESPONSE_MATCHING, CoapConfig.MatcherMode.RELAXED);
        networkConfig.set(CoapConfig.PREFERRED_BLOCK_SIZE, 1024);
        networkConfig.set(CoapConfig.MAX_MESSAGE_SIZE, 1024);
        networkConfig.set(CoapConfig.MAX_RETRANSMIT, 4);


        CoapEndpoint endpoint = new CoapEndpoint.Builder().setConfiguration(networkConfig)
                .build();
        client.setEndpoint(endpoint);
        client.useCONs(); // Used Confirmable request
        client.setTimeout(10000L); // Time out

        Request request = new Request(CoAP.Code.GET);
        request.setConfirmable(true); // Used Confirmable (CON)

        LOG.info("Send CoAP-request to [{}]", getPackageURI());

        client.advanced(new CoapHandler() {
            @Override
            public void onLoad(CoapResponse response) {
                byte[] payload = response.getPayload();
                if (payload != null && payload.length > 0) {
                    savePayloadToFile(payload, getPathOtaFW());
                } else {
                    LOG.error("An empty response or error was received.");
                }
            }

            @Override
            public void onError() {
                LOG.error("An error occurred while retrieving the response.");
            }
        }, request);


    }

    private void savePayloadToFile(byte[] data, String filePath) {
        try {
            // Make sure the directory exists
            Files.createDirectories(Paths.get(filePath).getParent());

            try (FileOutputStream fos = new FileOutputStream(new File(filePath))) {
                fos.write(data);
                LOG.info("Data successfully saved to: \"{}\", size: [{}]", filePath, data.length);
            }
        } catch (IOException e) {
            LOG.error("File writing error: ", e);
        }
    }

    private String getPathOtaFW() {
        if (this.otaFolder != null && this.fileNameFW != null) {
            return this.otaFolder + "/" + this.fileNameFW;
        } else {
            String retOtaFolder = this.otaFolder != null ? this.otaFolder : "null";
            String retFileNameFW = this.fileNameFW != null ? this.fileNameFW : "null";
            throw new IllegalStateException(
                    "--factory-bootstrap failed : unable to write resource: " + "ota FW" + " path: " + retOtaFolder + "/" + retFileNameFW);
        }
    }
}
