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
import org.eclipse.leshan.core.request.argument.Argument;
import org.eclipse.leshan.core.request.argument.Arguments;
import org.eclipse.leshan.core.response.ExecuteResponse;
import org.eclipse.leshan.core.response.ReadResponse;
import org.eclipse.leshan.core.response.WriteResponse;
import org.eclipse.leshan.core.util.StringUtils;
import org.thingsboard.lwm2m.demo.client.entities.LwM2MClientOtaInfo;
import org.thingsboard.lwm2m.demo.client.entities.OtaPackageType;
import org.thingsboard.lwm2m.demo.client.util.SoftwareUpdateResult;
import org.thingsboard.lwm2m.demo.client.util.SoftwareUpdateState;

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
import static org.thingsboard.lwm2m.demo.client.util.FirmwareUpdateState.DOWNLOADED;
import static org.thingsboard.lwm2m.demo.client.util.SoftwareUpdateResult.*;
import static org.thingsboard.lwm2m.demo.client.util.SoftwareUpdateState.*;
import static org.thingsboard.lwm2m.demo.client.util.Utils.*;

@Slf4j
public class SwLwM2MDevice extends BaseInstanceEnabler implements Destroyable {

    private static final List<Integer> supportedResources = Arrays.asList(0, 1, 2, 3, 4, 6, 7, 9);
    private static final String PACKAGE_NANE_DEF = "software";
    private static final String PACKAGE_VERSION_DEF = "1.0.0";

    private final ScheduledExecutorService scheduler = new ScheduledThreadPoolExecutor(1,
            new DaemonThreadFactory(getClass().getSimpleName() + "-test-scope"));
    private final AtomicInteger state = new AtomicInteger(0);

    private final AtomicInteger updateResult = new AtomicInteger(0);

    private final Timer timer;
    private boolean testObject;
    private boolean testOta;
    private String packageURI;
    private String packageName = PACKAGE_NANE_DEF;
    private String packageVersion = PACKAGE_VERSION_DEF;

    public SwLwM2MDevice() {
        this(5);
    }

    public SwLwM2MDevice(Integer timeDataFrequency) {
        this.initOtaSw();
        this.timer = new Timer("Id = [9] LWM2M Software Management -> schedule Time period = [" + timeDataFrequency + "] sec");
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                fireResourceChange(1);
                fireResourceChange(7);
                fireResourceChange(9);
            }
        }, timeDataFrequency*1000, timeDataFrequency*1000);
    }

    public SwLwM2MDevice(Integer timeDataFrequency, boolean testObject, boolean testOta) {
        this.testObject = testObject;
        this.testOta = testOta;
        this.initOtaSw();
        // notify new date each 5 second
        this.timer = new Timer("Id = [9] LWM2M Software Management -> schedule Time period = [" + timeDataFrequency + "] sec");
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                fireResourceChange(1);
                fireResourceChange(7);
                fireResourceChange(9);
            }
        }, timeDataFrequency*1000, timeDataFrequency*1000);
    }


    private void initOtaSw(){
        LwM2MClientOtaInfo infoSw = readOtaInfoFromFile(getPathInfoOtaSw());
        if (infoSw != null) {
            this.setPkgName(infoSw.getTitle());
            this.setPackageVersion(infoSw.getVersion());
        }
    }

    @Override
    public ReadResponse read(LwM2mServer server, int resourceId) {
        Object value;
        switch (resourceId) {
            case 0:
                value = getPkgName();
                printReadLog(server, getModel().name, getModel().id, getId(), resourceId, value);
                return ReadResponse.success(resourceId, (String) value);
            case 1:
                value = getPkgVersion();
                printReadLog(server, getModel().name, getModel().id, getId(), resourceId, value);
                return ReadResponse.success(resourceId, (String) value);
            case 7:
                value = getState();
                printReadLog(server, getModel().name, getModel().id, getId(), resourceId, value);
                return ReadResponse.success(resourceId, (int) value);
            case 9:
                value = getUpdateResult();
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
            case 4: // This Resource is only executable when the value of the State Resource is DELIVERED
                if (this.getState() == SoftwareUpdateState.DELIVERED.getCode() && this.getUpdateResult() == SoftwareUpdateResult.SUCCESSFULLY_DOWNLOADED_VERIFIED.getCode()) {
                    if (this.testObject || this.testOta) {
                        this.startUpdatingSw();
                    }
                    this.updatingSuccessTest();
                    return ExecuteResponse.success();
                } else {
                    String errorMsg = String.format("Firmware was updated failed. Sate: [%s] result: [%s]",
                            SoftwareUpdateState.fromUpdateStateSwByCode(this.getState()).getType(), SoftwareUpdateResult.fromUpdateResultSwByCode(this.getUpdateResult()).getType());
                    log.error(errorMsg);
                    return ExecuteResponse.badRequest(errorMsg);
                }
            case 6:
                if (!arguments.isEmpty()) {
                    int arg = ((Argument)arguments.getValues().toArray()[0]).getDigit();
                    if (arg == 0){
                        deleteSwFile();
                    } else if (arg == 1) {
                        // If the argument is 1 ("ForUpdate"), the Client MUST prepare itself for receiving a Package used to upgrade the Software already in place. Update State is set back to INITIAL state.
                        this.setState(SoftwareUpdateState.INITIAL.getCode());
                        this.setUpdateResult(SoftwareUpdateResult.INITIAL.getCode());
                    }
                } else {
                    deleteSwFile();
                }
                return ExecuteResponse.success();
            default:
                return super.execute(identity, resourceId, arguments);
        }
    }

    @Override
    public WriteResponse write(LwM2mServer identity, boolean replace, int resourceId, LwM2mResource value) {
        log.info("Write on Device resource /{}/{}/{}", getModel().id, getId(), resourceId);

        switch (resourceId) {
            case 2:
                if (this.testObject) {
                    this.downloadingToDownloadedSuccessTest(resourceId);
                    this.saveOtaInfoUpdateSwWithObject19((byte[]) value.getValue());
                } else if (this.testOta) {
                    String resultSavePayload = startDownloadingSw((byte[]) value.getValue());
                    if (!StringUtils.isEmpty(resultSavePayload)) {
                        return WriteResponse.badRequest(resultSavePayload);
                    }
                }
                return WriteResponse.success();
            case 3:
                this.setPackageURI((String) value.getValue());
                if (this.testObject) {
                    this.downloadingToDownloadedSuccessTest(resourceId);
                    this.saveOtaInfoUpdateSwWithObject19(null);
                } else if (this.testOta) {
                    this.startDownloadingSwUri();
                }
                return WriteResponse.success();
            default:
                return super.write(identity, replace, resourceId, value);
        }
    }

    private int getState() {
        return state.get();
    }

    private void setState(int state) {
        if (state != this.state.get()){
            this.state.set(state);
            log.info("Update state on Device resource /{}/{}/{} [{}] [{}]", getModel().id, getId(), 7, this.state.get(), SoftwareUpdateState.fromUpdateStateSwByCode(this.state.get()).getType());
            fireResourceChange(7);
        }

    }

    private int getUpdateResult() {
        return updateResult.get();
    }

    private void setUpdateResult(int updateResult) {
        if (updateResult != this.updateResult.get()) {
            this.updateResult.set(updateResult);
            log.info("Update result on Device resource /{}/{}/{} [{}] [{}]", getModel().id, getId(), 9, this.updateResult.get(), SoftwareUpdateResult.fromUpdateResultSwByCode(this.updateResult.get()).getType());
            fireResourceChange(9);
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

    @Override
    public List<Integer> getAvailableResourceIds(ObjectModel model) {
        return supportedResources;
    }

    @Override
    public void destroy() {
        scheduler.shutdownNow();
    }

    private void downloadingToDownloadedSuccessTest(int resourceId) {
        scheduler.schedule(() -> {
            try {
                this.setState(DOWNLOAD_STARTED.getCode());
                this.setUpdateResult(DOWNLOADING.getCode());
                Thread.sleep(100);

                this.setState(DOWNLOADED.getCode());
                Thread.sleep(100);

                this.setState(DELIVERED.getCode());
                this.setUpdateResult(SUCCESSFULLY_DOWNLOADED_VERIFIED.getCode());
            } catch (Exception e) {

            }
        }, 100, TimeUnit.MILLISECONDS);
        String msgResource = resourceId == 2 ? "Via resource 2." : "Via Resource 3 (PackageURI = " + this.getPackageURI() + ").";
        log.info("Finish Write data SW. {}", msgResource);
    }

    private void startUpdatingSw() {
        LwM2MClientOtaInfo infoSw = getOtaInfoUpdateSw();
        if (infoSw != null ) {
            writeOtaInfoToFile(getPathInfoOtaSw(), infoSw);
            this.setPkgName(infoSw.getTitle());
            this.setPackageVersion(infoSw.getVersion());
            setOtaInfoUpdateSw(null);
        }
    }

    private String startDownloadingSw(byte[] data) {
        this.setState(SoftwareUpdateState.DOWNLOAD_STARTED.getCode());
        this.setUpdateResult(SoftwareUpdateResult.DOWNLOADING.getCode());
        String result = "";
        if (data != null && data.length > 0) {
            LwM2MClientOtaInfo infoSw = getOtaInfoUpdateSw();
            if (infoSw != null ) {
                String fileChecksumSHA256 = Hashing.sha256().hashBytes(data).toString();
                if (!fileChecksumSHA256.equals(infoSw.getChecksum())) {
                    result = "File writing error: failed ChecksumSHA256. Payload: " + fileChecksumSHA256 + " Original: " + infoSw.getChecksum();
                    log.error(result);
                    this.updateResFailed(SoftwareUpdateResult.PACKAGE_CHECK_FAILURE.getCode());
                    return result;
                }
                if (data.length != infoSw.getDataSize()) {
                    result = "File writing error: failed FileSize.. Payload: " + data.length + " Original: " + infoSw.getDataSize();
                    log.error(result);
                    this.updateResFailed(SoftwareUpdateResult.PACKAGE_CHECK_FAILURE.getCode());
                    return result;
                }
            } else {
                this.saveOtaInfoUpdateSwWithObject19(data);
            }
            this.setState(SoftwareUpdateState.DOWNLOADED.getCode());
            String filePath = getPathDataOtaSW(infoSw);
            Path dirPath = Paths.get(filePath).getParent();
            try {
                Files.createDirectories(dirPath);
                renameOtaFilesToTmp(dirPath, PREF_SW, PREF_TMP);
                try (FileOutputStream fos = new FileOutputStream(filePath)) {
                    fos.write(data);
                    log.info("Data successfully saved to: \"{}\", size: [{}]", filePath, data.length);
                    deleteOtaFiles(dirPath, PREF_TMP);
                    this.setState(SoftwareUpdateState.DELIVERED.getCode());
                    this.setUpdateResult(SUCCESSFULLY_DOWNLOADED_VERIFIED.getCode());
                    return result;
                }

            } catch (IOException e) {
                result = "File writing error: " + e.getMessage();
                log.error("File writing error: ", e);
                this.updateResFailed(SoftwareUpdateResult.OUT_OFF_MEMORY.getCode());
                return result;
            }
        } else {
            result = "An empty response or error was received.";
            log.error(result);
            this.updateResFailed(SoftwareUpdateResult.PACKAGE_CHECK_FAILURE.getCode());
            return result;
        }
    }

    private void updatingSuccessTest() {
        scheduler.schedule(() -> {
            try {
                this.setState(SoftwareUpdateState.INSTALLED.getCode());
                Thread.sleep(100);
                this.setUpdateResult(SoftwareUpdateResult.SUCCESSFULLY_INSTALLED.getCode());
                Thread.sleep(100);
                this.setState(SoftwareUpdateState.INITIAL.getCode());
                this.setUpdateResult(SoftwareUpdateResult.INITIAL.getCode());
            } catch (Exception e) {
            }
        }, 100, TimeUnit.MILLISECONDS);
    }


    private void updateResFailed(int res) {
        scheduler.schedule(() -> {
            try {
                this.setUpdateResult(res);
                Thread.sleep(100);
                this.setState(SoftwareUpdateState.INITIAL.getCode());
                this.setUpdateResult(SoftwareUpdateResult.INITIAL.getCode());
            } catch (Exception e) {
            }
        }, 100, TimeUnit.MILLISECONDS);
    }

    private void startDownloadingSwUri() {
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
                String resultSavePayload = startDownloadingSw(payload);
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


    private String getPathDataOtaSW(LwM2MClientOtaInfo infoSW) {
        String fileName = infoSW == null || StringUtils.isEmpty(infoSW.getFileName()) ? SW_DATA_FILE_NANE_DEF : infoSW.getFileName();
        return getOtaFolder() + "/" + fileName;
    }


    private void saveOtaInfoUpdateSwWithObject19(byte[] data) {
        if (data != null && data.length > 0) {
            LwM2MClientOtaInfo infoSw = getOtaInfoUpdateSw();
            if (infoSw == null ) {
                String fileChecksumSHA256 = Hashing.sha256().hashBytes(data).toString();
                infoSw = new LwM2MClientOtaInfo();
                infoSw.setType(OtaPackageType.SOFTWARE);
                infoSw.setFileName(SW_DATA_FILE_NANE_DEF);
                infoSw.setChecksum(fileChecksumSHA256);
                infoSw.setDataSize(data.length);
                setOtaInfoUpdateSw(infoSw);
                log.info("Create new SW info with default params.");
            }
        } else {
            setOtaInfoUpdateSw(null);
            log.info("New SW info is not Created with default params (PackageURI + testObject). data = null");
            String path = getOtaFolder();
            deleteOtaFiles(Paths.get(path), PREF_SW);
            log.info("Delete all SW files from path: [{}/{}...]", path, PREF_SW);
        }
    }


    /**
     * This executable resource may have one argument.
     * If used with no argument or argument is 0, the Package is removed i from the Device.
     */
    private void deleteSwFile(){
        LwM2MClientOtaInfo infoSw = readOtaInfoFromFile(getPathInfoOtaSw());
        if (infoSw != null) {
            String filePath = getPathDataOtaSW(infoSw);
            Path path = Paths.get(filePath);
            deleteOtaFiles(path.getParent(), path.getFileName().toString());
            log.info("[{}] successfully is removed from the Device", filePath);
        }
    }
}
