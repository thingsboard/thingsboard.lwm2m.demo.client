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
package org.thingsboard.lwm2m.demo.client.objects;

import com.google.common.hash.Hashing;
import lombok.extern.slf4j.Slf4j;
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
import org.thingsboard.lwm2m.demo.client.entities.LwM2MClientOtaInfo;
import org.thingsboard.lwm2m.demo.client.entities.OtaPackageType;
import org.thingsboard.lwm2m.demo.client.util.FirmwareUpdateResult;
import org.thingsboard.lwm2m.demo.client.util.FirmwareUpdateState;
import org.thingsboard.lwm2m.demo.client.util.Utils;

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

@Slf4j
public class FwLwM2MDevice extends BaseInstanceEnabler implements Destroyable {

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
        this(5);
    }

    public FwLwM2MDevice(Integer timeDataFrequency) {
        // notify new date each 5 second
        this.initOtaFw();
        this.timer = new Timer("Id = [5] Firmware Update -> schedule Time period = [" + timeDataFrequency + "] sec");
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                fireResourceChange(1);
                fireResourceChange(3);
                fireResourceChange(5);
            }
        }, timeDataFrequency*1000, timeDataFrequency*1000);
    }

    public FwLwM2MDevice(Integer timeDataFrequency, boolean testObject, boolean testOta) {
        this.testObject = testObject;
        this.testOta = testOta;
        this.initOtaFw();
        // notify new date each 5 second
        this.timer = new Timer("Id = [5] Firmware Update -> schedule Time period = [" + timeDataFrequency + "] sec");
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                fireResourceChange(1);
                fireResourceChange(3);
                fireResourceChange(5);
            }
        }, timeDataFrequency*1000, timeDataFrequency*1000);
    }

    private void initOtaFw(){
        LwM2MClientOtaInfo infoFw = readOtaInfoFromFile(getPathInfoOtaFw());
        if (infoFw != null) {
            this.setPkgName(infoFw.getTitle());
            this.setPackageVersion(infoFw.getVersion());
        }
    }


    @Override
    public ReadResponse read(LwM2mServer server, int resourceId) {
        Object value;
        switch (resourceId) {
            case 1:
                value = getPackageURI();
                printReadLog(server, getModel().name, getModel().id, getId(), resourceId, value);
                return ReadResponse.success(resourceId, (String) value);
            case 3:
                value = getState();
                printReadLog(server, getModel().name, getModel().id, getId(), resourceId, value);
                return ReadResponse.success(resourceId, (int) value);
            case 5:
                value = getUpdateResult();
                printReadLog(server, getModel().name, getModel().id, getId(), resourceId, value);
                return ReadResponse.success(resourceId, (int) value);
            case 6:
                value = getPkgName();
                printReadLog(server, getModel().name, getModel().id, getId(), resourceId, value);
                return ReadResponse.success(resourceId, (String) value);
            case 7:
                value = getPkgVersion();
                printReadLog(server, getModel().name, getModel().id, getId(), resourceId, value);
                return ReadResponse.success(resourceId, (String) value);
            case 9:
                value = getFirmwareUpdateDeliveryMethod();
                printReadLog(server, getModel().name, getModel().id, getId(), resourceId, value);
                return ReadResponse.success(resourceId, (int) value);
            default:
                return super.read(server, resourceId);
        }

    }

    @Override
    public ExecuteResponse execute(LwM2mServer identity, int resourceId, Arguments arguments) {
        String withArguments = "";
        if (!arguments.isEmpty())
            withArguments = " with arguments " + arguments;
        log.info("Execute on Device resource /{}/{}/{} {}", getModel().id, getId(), resourceId, withArguments);

        switch (resourceId) {
            case 2:
                if (this.getState() == FirmwareUpdateState.DOWNLOADED.getCode() && this.getUpdateResult() == FirmwareUpdateResult.INITIAL.getCode()) {
                    if (this.testObject || this.testOta) {
                        startUpdatingFw();
                    }
                    this.updatingSuccessTest();
                    return ExecuteResponse.success();
                } else {
                    String errorMsg = String.format("Firmware was updated failed. Sate: [%s] result: [%s]", FirmwareUpdateState.fromCode(this.getState()).getType(), FirmwareUpdateResult.fromCode(this.getUpdateResult()).getType());
                    log.error(errorMsg);
                    return ExecuteResponse.badRequest(errorMsg);
                }
            default:
                return super.execute(identity, resourceId, arguments);
        }
    }

    @Override
    public WriteResponse write(LwM2mServer identity, boolean replace, int resourceId, LwM2mResource value) {
        log.info("Write on Device resource /{}/{}/{}", getModel().id, getId(), resourceId);

        switch (resourceId) {
            case 0:
                if (this.testObject) {
                    this.downloadingToDownloadedSuccessTest(resourceId);
                    this.saveOtaInfoUpdateFwWithObject19((byte[]) value.getValue());
                } else if (this.testOta) {
                    String resultSavePayload = startDownloadingFw((byte[]) value.getValue());
                    if (!StringUtils.isEmpty(resultSavePayload)) {
                        return WriteResponse.badRequest(resultSavePayload);
                    }
                }
                return WriteResponse.success();
            case 1:
                this.setPackageURI((String) value.getValue());
                if (this.testObject) {
                    this.downloadingToDownloadedSuccessTest(resourceId);
                    this.saveOtaInfoUpdateFwWithObject19(null);
                } else if (this.testOta) {
                    this.startDownloadingFwUri();
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
            log.info("Write on Device packageURI: [{}]", packageURI);
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
            log.info("Update state FW on Device resource /{}/{}/{} [{}] [{}]", getModel().id, getId(), 3, this.state.get(), FirmwareUpdateState.fromCode(this.state.get()).getType());
            fireResourceChange(3);
        }

    }

    private int getUpdateResult() {
        return updateResult.get();
    }
    private void setUpdateResult(int updateResult) {
        if (updateResult != this.updateResult.get()) {
            this.updateResult.set(updateResult);
            log.info("Update result FW on Device resource /{}/{}/{} [{}] [{}]", getModel().id, getId(), 3, this.state.get(), FirmwareUpdateResult.fromCode(this.updateResult.get()).getType());
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

    private void downloadingToDownloadedSuccessTest(int resourceId) {
        scheduler.schedule(() -> {
            try {
                this.setState(FirmwareUpdateState.DOWNLOADING.getCode());
                Thread.sleep(100);
                this.setState(FirmwareUpdateState.DOWNLOADED.getCode());
            } catch (Exception e) {
            }
        }, 100, TimeUnit.MILLISECONDS);
        String msgResource = resourceId == 0 ? "Via resource 0." : "Via Resource 1 (PackageURI = " + this.getPackageURI() + ").";
        log.info("Finish Write data FW. {}", msgResource);
    }

    private void updatingSuccessTest() {
        scheduler.schedule(() -> {
            try {
                this.setState(FirmwareUpdateState.UPDATING.getCode());
                Thread.sleep(100);
                this.setUpdateResult(FirmwareUpdateResult.SUCCESS.getCode());
                Thread.sleep(100);
                this.setState(FirmwareUpdateState.IDLE.getCode());
                this.setUpdateResult(FirmwareUpdateResult.INITIAL.getCode());
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

    private void startDownloadingFwUri() {
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

        log.info("Send CoAP-request to [{}]", getPackageURI());

        client.advanced(new CoapHandler() {
            @Override
            public void onLoad(CoapResponse response) {
                byte[] payload = response.getPayload();
                String resultSavePayload = startDownloadingFw(payload);
                if (!resultSavePayload.isEmpty()) {
                    log.error(resultSavePayload);
                }
            }

            @Override
            public void onError() {
                log.error("An error occurred while retrieving the response.");
            }
        }, request);
    }

    private void startUpdatingFw() {
        LwM2MClientOtaInfo infoFw = getOtaInfoUpdateFw();
        if (infoFw != null ) {
            writeOtaInfoToFile(getPathInfoOtaFw(), infoFw);
            this.setPkgName(infoFw.getTitle());
            this.setPackageVersion(infoFw.getVersion());
            setOtaInfoUpdateFw(null);
        }
    }

    private String startDownloadingFw(byte[] data) {
        this.setState(FirmwareUpdateState.DOWNLOADING.getCode());
        String result = "";
        if (data != null && data.length > 0) {
            LwM2MClientOtaInfo infoFw = getOtaInfoUpdateFw();
            if (infoFw != null ) {
                String fileChecksumSHA256 = Hashing.sha256().hashBytes(data).toString();
                if (!fileChecksumSHA256.equals(infoFw.getChecksum())) {
                    result = "File writing error: failed ChecksumSHA256. Payload: " + fileChecksumSHA256 + " Original: " + infoFw.getChecksum();
                    log.error(result);
                    // 5: Integrity check failure for new downloaded package.
                    this.updateResFailed(FirmwareUpdateResult.INTEGRITY_CHECK_FAILURE.getCode());
                    return result;
                }
                if (data.length != infoFw.getDataSize()) {
                    result = "File writing error: failed FileSize.. Payload: " + data.length + " Original: " + infoFw.getDataSize();
                    log.error(result);
                    // 5: Integrity check failure for new downloaded package.
                    this.updateResFailed(FirmwareUpdateResult.INTEGRITY_CHECK_FAILURE.getCode());
                    return result;
                }
            } else {
                this.saveOtaInfoUpdateFwWithObject19(data);
            }
            String filePath = getPathDataOtaFW(infoFw);
            Path dirPath = Paths.get(filePath).getParent();
            try {
                Files.createDirectories(dirPath);
                renameOtaFilesToTmp(dirPath, PREF_FW, PREF_TMP);
                try (FileOutputStream fos = new FileOutputStream(filePath)) {
                    fos.write(data);
                    log.info("Data successfully saved to: \"{}\", size: [{}]", filePath, data.length);
                    this.setState(FirmwareUpdateState.DOWNLOADED.getCode());
                    deleteOtaFiles(dirPath, PREF_TMP);
                    return result;
                }

            } catch (IOException e) {
                result = "File writing error: " + e.getMessage();
                log.error("File writing error: ", e);
                this.updateResFailed(FirmwareUpdateResult.NOT_ENOUGH_FLASH.getCode());
                return result;
            }
        } else {
            result = "An empty response or error was received.";
            log.error(result);
            this.updateResFailed(FirmwareUpdateResult.INTEGRITY_CHECK_FAILURE.getCode());
            return result;
        }
    }

    private String getPathDataOtaFW(LwM2MClientOtaInfo infoFW) {
        String fileName = infoFW == null || StringUtils.isEmpty(infoFW.getFileName()) ? FW_DATA_FILE_NANE_DEF : infoFW.getFileName();
        return getOtaFolder() + "/" + fileName;
    }

    private void saveOtaInfoUpdateFwWithObject19(byte[] data) {
        if (data != null && data.length > 0) {
            LwM2MClientOtaInfo infoFw = getOtaInfoUpdateFw();
            if (infoFw == null ) {
                String fileChecksumSHA256 = Hashing.sha256().hashBytes(data).toString();
                infoFw = new LwM2MClientOtaInfo();
                infoFw.setType(OtaPackageType.FIRMWARE);
                infoFw.setFileName(FW_DATA_FILE_NANE_DEF);
                infoFw.setChecksum(fileChecksumSHA256);
                infoFw.setDataSize(data.length);
                setOtaInfoUpdateFw(infoFw);
                log.info("Create new FW info with default params.");
            }
        } else {
            setOtaInfoUpdateFw(null);
            log.info("New FW info is not Created with default params (PackageURI + testObject). data = null");
            String path = getOtaFolder();
            deleteOtaFiles(Paths.get(path), PREF_FW);
            log.info("Delete all FW files from path: [{}/{}...]", path, PREF_FW);
        }
    }
}
