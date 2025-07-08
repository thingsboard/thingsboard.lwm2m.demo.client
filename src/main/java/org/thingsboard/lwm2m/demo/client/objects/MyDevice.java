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

import lombok.extern.slf4j.Slf4j;
import org.eclipse.leshan.client.resource.BaseInstanceEnabler;
import org.eclipse.leshan.client.servers.LwM2mServer;
import org.eclipse.leshan.core.Destroyable;
import org.eclipse.leshan.core.model.ObjectModel;
import org.eclipse.leshan.core.model.ResourceModel.Type;
import org.eclipse.leshan.core.node.LwM2mResource;
import org.eclipse.leshan.core.request.BindingMode;
import org.eclipse.leshan.core.request.argument.Arguments;
import org.eclipse.leshan.core.response.ExecuteResponse;
import org.eclipse.leshan.core.response.ReadResponse;
import org.eclipse.leshan.core.response.WriteResponse;
import org.eclipse.leshan.core.util.StringUtils;
import org.thingsboard.lwm2m.demo.client.entities.LwM2MClientOtaInfo;
import org.thingsboard.lwm2m.demo.client.util.Utils;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

import static org.thingsboard.lwm2m.demo.client.util.Utils.getPathInfoOtaFw;
import static org.thingsboard.lwm2m.demo.client.util.Utils.printReadLog;
import static org.thingsboard.lwm2m.demo.client.util.Utils.readOtaInfoFromFile;

@Slf4j
public class MyDevice extends BaseInstanceEnabler implements Destroyable {

    private static final Random RANDOM = new Random();
    private static final List<Integer> supportedResources = Arrays.asList(0, 1, 2, 3, 9, 10, 11, 13, 14, 15, 16, 17, 18,
            19, 20, 21);

    private final Timer timer;
    private String firmwareVersion;

    public MyDevice() {
        this(5);
    }

    public MyDevice(Integer timeDataFrequency) {
        this.initOtaFw();
        // notify new date each 5 second Default
        this.timer = new Timer("Id = [3] Device -> schedule Time period = [" + timeDataFrequency + "] sec");
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                fireResourceChange(13);
                fireResourceChange(9);
            }
        }, timeDataFrequency*1000, timeDataFrequency*1000);
    }

    private void initOtaFw() {
        LwM2MClientOtaInfo infoFw = readOtaInfoFromFile(getPathInfoOtaFw());
        if (infoFw != null) {
            this.setFirmwareVersion(infoFw.getVersion());
        }
    }

    @Override
    public ReadResponse read(LwM2mServer server, int resourceId) {
        Object value;
        switch (resourceId) {
            case 0:
                value = getManufacturer();
                printReadLog(server, getModel().name, getModel().id, getId(), resourceId, value);
                return ReadResponse.success(resourceId, (String) value);
            case 1:
                value = getModelNumber();
                printReadLog(server, getModel().name, getModel().id, getId(), resourceId, value);
                return ReadResponse.success(resourceId, (String) value);
            case 2:
                value = getSerialNumber();
                printReadLog(server, getModel().name, getModel().id, getId(), resourceId, value);
                return ReadResponse.success(resourceId, (String) value);
            case 3:
                value = getFirmwareVersion();
                printReadLog(server, getModel().name, getModel().id, getId(), resourceId, value);
                return ReadResponse.success(resourceId, (String) value);
            case 9:
                value = getBatteryLevel();
                printReadLog(server, getModel().name, getModel().id, getId(), resourceId, value);
                return ReadResponse.success(resourceId, (int) value);
            case 10:
                value = getMemoryFree();
                printReadLog(server, getModel().name, getModel().id, getId(), resourceId, value);
                return ReadResponse.success(resourceId, (long) value);
            case 11:
                Map<Integer, Long> errorCodes = new HashMap<>();
                errorCodes.put(0, getErrorCode());
                printReadLog(server, getModel().name, getModel().id, getId(), resourceId, errorCodes);
                return ReadResponse.success(resourceId, errorCodes, Type.INTEGER);
            case 13:
                value = getCurrentTime();
                printReadLog(server, getModel().name, getModel().id, getId(), resourceId, value);
                return ReadResponse.success(resourceId, (Date) value);
            case 14:
                value = getUtcOffset();
                printReadLog(server, getModel().name, getModel().id, getId(), resourceId, value);
                return ReadResponse.success(resourceId, (String) value);
            case 15:
                value = getTimezone();
                printReadLog(server, getModel().name, getModel().id, getId(), resourceId, value);
                return ReadResponse.success(resourceId, (String) value);
            case 16:
                value = getSupportedBinding();
                printReadLog(server, getModel().name, getModel().id, getId(), resourceId, value);
                return ReadResponse.success(resourceId, (String) value);
            case 17:
                value = getDeviceType();
                printReadLog(server, getModel().name, getModel().id, getId(), resourceId, value);
                return ReadResponse.success(resourceId, (String) value);
            case 18:
                value = getHardwareVersion();
                printReadLog(server, getModel().name, getModel().id, getId(), resourceId, value);
                return ReadResponse.success(resourceId, (String) value);
            case 19:
                value = getSoftwareVersion();
                printReadLog(server, getModel().name, getModel().id, getId(), resourceId, value);
                return ReadResponse.success(resourceId, (String) value);
            case 20:
                value = getBatteryStatus();
                printReadLog(server, getModel().name, getModel().id, getId(), resourceId, value);
                return ReadResponse.success(resourceId, (int) value);
            case 21:
                value = getMemoryTotal();
                printReadLog(server, getModel().name, getModel().id, getId(), resourceId, value);
                return ReadResponse.success(resourceId, (long) value);
            default:
                return super.read(server, resourceId);
        }
    }

    @Override
    public ExecuteResponse execute(LwM2mServer server, int resourceid, Arguments arguments) {
        String withArguments = "";
        if (!arguments.isEmpty())
            withArguments = " with arguments " + arguments;
        log.info("Execute on Device resource /{}/{}/{} {}", getModel().id, getId(), resourceid, withArguments);

        if (resourceid == 4) {
            this.triggerRebootClient();
        }
        return ExecuteResponse.success();
    }

    @Override
    public WriteResponse write(LwM2mServer server, boolean replace, int resourceid, LwM2mResource value) {
        log.info("Write on Device resource /{}/{}/{}", getModel().id, getId(), resourceid);

        switch (resourceid) {
            case 13:
                return WriteResponse.notFound();
            case 14:
                setUtcOffset((String) value.getValue());
                fireResourceChange(resourceid);
                return WriteResponse.success();
            case 15:
                setTimezone((String) value.getValue());
                fireResourceChange(resourceid);
                return WriteResponse.success();
            default:
                return super.write(server, replace, resourceid, value);
        }
    }

    private String getManufacturer() {
        return "Leshan Demo Device";
    }

    private String getModelNumber() {
        return "Model 500";
    }

    private String getSerialNumber() {
        return "LT-500-000-0001";
    }

    private String getFirmwareVersion() {
        return StringUtils.isEmpty(this.firmwareVersion) ? "1.0.0" : this.firmwareVersion;
    }

    private void setFirmwareVersion(String firmwareVersion) {
        this.firmwareVersion = firmwareVersion;
    }

    private long getErrorCode() {
        return 0;
    }

    private int getBatteryLevel() {
        return RANDOM.nextInt(101);
    }

    private long getMemoryFree() {
        return Runtime.getRuntime().freeMemory() / 1024;
    }

    private Date getCurrentTime() {
        return new Date();
    }

    private String utcOffset = new SimpleDateFormat("X").format(Calendar.getInstance().getTime());

    private String getUtcOffset() {
        return utcOffset;
    }

    private void setUtcOffset(String t) {
        utcOffset = t;
    }

    private String timeZone = TimeZone.getDefault().getID();

    private String getTimezone() {
        return timeZone;
    }

    private void setTimezone(String t) {
        timeZone = t;
    }

    private String getSupportedBinding() {
        return BindingMode.toString(EnumSet.of(BindingMode.U, BindingMode.T));
    }

    private String getDeviceType() {
        return "Demo";
    }

    private String getHardwareVersion() {
        return "1.0.1";
    }

    private String getSoftwareVersion() {
        return "1.0.2";
    }

    private int getBatteryStatus() {
        return RANDOM.nextInt(7);
    }

    private long getMemoryTotal() {
        return Runtime.getRuntime().totalMemory() / 1024;
    }

    @Override
    public List<Integer> getAvailableResourceIds(ObjectModel model) {
        return supportedResources;
    }

    public void triggerRebootClient() {
        new Timer("Reboot LwM2MClient").schedule(new TimerTask() {
            @Override
            public void run() {
                getLwM2mClient().stop(true);
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                }
                getLwM2mClient().start();
            }
        }, 500);
    }

    @Override
    public void destroy() {
        timer.cancel();
    }
}
