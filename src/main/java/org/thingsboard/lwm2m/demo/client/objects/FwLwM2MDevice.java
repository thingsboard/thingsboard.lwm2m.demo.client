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


import com.google.common.hash.Hashing;
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
import org.eclipse.leshan.core.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thingsboard.lwm2m.demo.client.entities.LwM2MClientOtaInfo;
import org.thingsboard.lwm2m.demo.client.entities.OtaPackageType;
import org.thingsboard.lwm2m.demo.client.util.FirmwareUpdateResult;
import org.thingsboard.lwm2m.demo.client.util.FirmwareUpdateState;

import javax.security.auth.Destroyable;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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
import static org.thingsboard.lwm2m.demo.client.util.Utils.*;

public class FwLwM2MDevice extends BaseInstanceEnabler implements Destroyable {

    private static final Logger LOG = LoggerFactory.getLogger(FwLwM2MDevice.class);
    private static final List<Integer> supportedResources = Arrays.asList(0, 1, 2, 3, 5, 6, 7, 9);
    private static final String PACKAGE_NANE_DEF = "firmware";
    private static final String PACKAGE_VERSION_DEF = "1.0.0";

    private final ScheduledExecutorService scheduler = new ScheduledThreadPoolExecutor(1,
            new DaemonThreadFactory(getClass().getSimpleName() + "-test-scope-delete-ota"));
    private final AtomicInteger state = new AtomicInteger(0);

    private final AtomicInteger updateResult = new AtomicInteger(0);
    private final Timer timer;
    private boolean testObject;
    private boolean testOta;
    private String packageURI;
    private String packageName = PACKAGE_NANE_DEF;
    private String packageVersion = PACKAGE_VERSION_DEF;

    public FwLwM2MDevice() {
        // notify new date each 5 second
        this.initOtaFw();
        this.timer = new Timer("5 - Device-Current Time");
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
//                fireResourceChange(1);
//                fireResourceChange(9);
            }
        }, 5000, 5000);
    }

    public FwLwM2MDevice(boolean testObject, boolean testOta) {
        this.testObject = testObject;
        this.testOta = testOta;
        this.initOtaFw();
        // notify new date each 5 second
        this.timer = new Timer("5 - Device-Current Time");
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
//                fireResourceChange(1);
//                fireResourceChange(9);
            }
        }, 5000, 5000);
    }

    private void initOtaFw(){
        LwM2MClientOtaInfo infoFw = readOtaInfoFromFile(getPathInfoOtaFw());
        if (infoFw != null) {
            this.setPkgName(infoFw.getTitle());
            this.setPackageVersion(infoFw.getVersion());
        }
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
                if (this.getState() == FirmwareUpdateState.DOWNLOADED.getCode() && this.getUpdateResult() == FirmwareUpdateResult.INITIAL.getCode()) {
                    if (this.testObject) {
                        this.updatingSuccessTest();
                        return ExecuteResponse.success();
                    } else if (this.testOta) {
                            this.startUpdating();
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
//                            throw new RuntimeException(e);
                        }
                        this.setState(FirmwareUpdateState.IDLE.getCode());
                        this.setUpdateResult(FirmwareUpdateResult.INITIAL.getCode());

                        return ExecuteResponse.success();
                    }
                } else {
                    String errorMsg = String.format("Firmware was updated failed. Sate: [%s] result: [%s]", FirmwareUpdateState.fromCode(this.getState()).getDescription(), FirmwareUpdateResult.fromCode(this.getUpdateResult()).getDescription());
                    LOG.error(errorMsg);
                    return ExecuteResponse.badRequest(errorMsg);
                }
            default:
                return super.execute(identity, resourceId, arguments);
        }
    }

    @Override
    public WriteResponse write(LwM2mServer identity, boolean replace, int resourceId, LwM2mResource value) {
        LOG.info("Write on Device resource /{}/{}/{}", getModel().id, getId(), resourceId);

        switch (resourceId) {
            case 0:
                if (this.testObject) {
                    this.downloadingToDownloadedSuccessTest();
                } else if (this.testOta) {
                    String resultSavePayload = startDownloading((byte[]) value.getValue());
                    if (!StringUtils.isEmpty(resultSavePayload)) {
                        return WriteResponse.badRequest(resultSavePayload);
                    }
                }
                return WriteResponse.success();
            case 1:
                this.setPackageURI((String) value.getValue());
                if (this.testObject) {
                    this.downloadingToDownloadedSuccessTest();
                } else if (this.testOta) {
                    this.startDownloadingUri();
                }
                return WriteResponse.success();
            default:
                return super.write(identity, replace, resourceId, value);
        }
    }

    private String getPackageURI() {
        return packageURI == null ? "" : packageURI;
    }

    private void setPackageURI(String packageURI) {
        if (!packageURI.equals(this.packageURI)) {
            fireResourceChange(1);
        }
        this.packageURI = packageURI;
    }

    private int getState() {
        return state.get();
    }

    private void setState(int state) {
        if (state != this.state.get()){
            this.state.set(state);
            fireResourceChange(3);
        }

    }

    private int getUpdateResult() {
        return updateResult.get();
    }
    private void setUpdateResult(int updateResult) {
        if (updateResult != this.updateResult.get()) {
            this.updateResult.set(updateResult);
            fireResourceChange(5);
        }
    }

    private String getPkgName() {
        return this.packageName;
    }

    private void setPkgName(String packageName) {
        this.packageName = packageName == null ? PACKAGE_NANE_DEF : packageName;
    }

    private String getPkgVersion() {
        return this.packageVersion;
    }

    private void setPackageVersion(String packageVersion){
        this.packageVersion = packageVersion == null ? PACKAGE_VERSION_DEF : packageVersion;
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

    private void downloadingToDownloadedSuccessTest() {
        scheduler.schedule(() -> {
            try {
                this.setState(FirmwareUpdateState.DOWNLOADING.getCode());
                Thread.sleep(100);
                this.setState(FirmwareUpdateState.DOWNLOADED.getCode());
            } catch (Exception e) {
            }
        }, 100, TimeUnit.MILLISECONDS);
    }

    private void updatingSuccessTest() {
        scheduler.schedule(() -> {
            try {
                this.setState(FirmwareUpdateState.UPDATING.getCode());
                Thread.sleep(100);
                this.setUpdateResult(FirmwareUpdateResult.SUCCESS.getCode());
            } catch (Exception e) {
            }
        }, 100, TimeUnit.MILLISECONDS);
    }

    private void updateResFailed(int res) {
        scheduler.schedule(() -> {
            try {
                this.setUpdateResult(res);
                Thread.sleep(100);
                this.setState(FirmwareUpdateState.IDLE.getCode());
                this.setUpdateResult(FirmwareUpdateResult.INITIAL.getCode());
            } catch (Exception e) {
            }
        }, 100, TimeUnit.MILLISECONDS);
    }

    private void startDownloadingUri() {
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
                String resultSavePayload = startDownloading(payload);
                if (!resultSavePayload.isEmpty()) {
                    LOG.error(resultSavePayload);
                }
            }

            @Override
            public void onError() {
                LOG.error("An error occurred while retrieving the response.");
            }
        }, request);
    }

    private void startUpdating() {
        LwM2MClientOtaInfo infoFw = getOtaInfoUpdateFw();
        if (infoFw != null ) {
            writeOtaInfoToFile(getPathInfoOtaFw(), infoFw);
            this.setPkgName(infoFw.getTitle());
            this.setPackageVersion(infoFw.getVersion());
            setOtaInfoUpdateFw(null);
        }
        this.setState(FirmwareUpdateState.UPDATING.getCode());
        this.setUpdateResult(FirmwareUpdateResult.SUCCESS.getCode());
    }

    private String startDownloading(byte[] data) {
        this.setState(FirmwareUpdateState.DOWNLOADING.getCode());
        String result = "";
        if (data != null && data.length > 0) {
            LwM2MClientOtaInfo infoFw = getOtaInfoUpdateFw();
            String fileChecksumSHA256 = Hashing.sha256().hashBytes(data).toString();
            if (infoFw != null ) {
                if (!fileChecksumSHA256.equals(infoFw.getFileChecksumSHA256())) {
                    result = "File writing error: failed ChecksumSHA256. Payload: " + fileChecksumSHA256 + " Original: " + infoFw.getFileChecksumSHA256();
                    LOG.error(result);
                    // 5: Integrity check failure for new downloaded package.
                    this.updateResFailed(FirmwareUpdateResult.INTEGRITY_CHECK_FAILURE.getCode());
                    return result;
                }
                if (data.length != infoFw.getFileSize()) {
                    result = "File writing error: failed FileSize.. Payload: " + data.length + " Original: " + infoFw.getFileSize();
                    LOG.error(result);
                    // 5: Integrity check failure for new downloaded package.
                    this.updateResFailed(FirmwareUpdateResult.INTEGRITY_CHECK_FAILURE.getCode());
                    return result;
                }
            } else {
                infoFw = new LwM2MClientOtaInfo();
                infoFw.setPackageType(OtaPackageType.FIRMWARE);
                infoFw.setFileName(FW_DATA_FILE_NANE_DEF);
                infoFw.setFileChecksumSHA256(fileChecksumSHA256);
                infoFw.setFileSize(data.length);
                setOtaInfoUpdateFw(infoFw);
                LOG.info("Create new FW info with default params.");
            }
            String filePath = getPathDataOtaFW(infoFw);
            Path dirPath = Paths.get(filePath).getParent();
            try {
                Files.createDirectories(dirPath);
                String prefTmp = "_tmp";
                renameOtaFilesToTmp(dirPath, "FW", prefTmp);
                try (FileOutputStream fos = new FileOutputStream(filePath)) {
                    fos.write(data);
                    LOG.info("Data successfully saved to: \"{}\", size: [{}]", filePath, data.length);
                    this.setState(FirmwareUpdateState.DOWNLOADED.getCode());
                    deleteOtaFiles(dirPath, prefTmp);
                    return result;
                }

            } catch (IOException e) {
                result = "File writing error: " + e.getMessage();
                LOG.error("File writing error: ", e);
                this.updateResFailed(FirmwareUpdateResult.NOT_ENOUGH_FLASH.getCode());
                return result;
            }
        } else {
            result = "An empty response or error was received.";
            LOG.error(result);
            this.updateResFailed(FirmwareUpdateResult.INTEGRITY_CHECK_FAILURE.getCode());
            return result;
        }
    }

    private String getPathDataOtaFW(LwM2MClientOtaInfo infoFW) {
        String fileName = infoFW == null || StringUtils.isEmpty(infoFW.getFileName()) ? FW_DATA_FILE_NANE_DEF : infoFW.getFileName();
        return getOtaFolder() + "/" + fileName;
    }
}
