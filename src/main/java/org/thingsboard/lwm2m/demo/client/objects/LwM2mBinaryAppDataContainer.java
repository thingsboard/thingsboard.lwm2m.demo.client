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

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.leshan.client.resource.BaseInstanceEnabler;
import org.eclipse.leshan.client.servers.LwM2mServer;
import org.eclipse.leshan.core.model.ObjectModel;
import org.eclipse.leshan.core.model.ResourceModel;
import org.eclipse.leshan.core.node.LwM2mMultipleResource;
import org.eclipse.leshan.core.node.LwM2mResource;
import org.eclipse.leshan.core.response.ReadResponse;
import org.eclipse.leshan.core.response.WriteResponse;
import org.thingsboard.lwm2m.demo.client.entities.LwM2MClientOtaInfo;
import org.thingsboard.lwm2m.demo.client.entities.OtaPackageType;
import org.thingsboard.lwm2m.demo.client.util.Utils;

import javax.security.auth.Destroyable;
import java.sql.Time;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.*;

import static org.thingsboard.lwm2m.demo.client.util.Utils.*;

@Slf4j
public class LwM2mBinaryAppDataContainer extends BaseInstanceEnabler implements Destroyable {

    /**
     * id = 0
     * Multiple
     * base64
     */

    /**
     * Example1:
     * InNlcnZpY2VJZCI6Ik1ldGVyIiwNCiJzZXJ2aWNlRGF0YSI6ew0KImN1cnJlbnRSZWFka
     * W5nIjoiNDYuMyIsDQoic2lnbmFsU3RyZW5ndGgiOjE2LA0KImRhaWx5QWN0aXZpdHlUaW1lIjo1NzA2DQo=
     * "serviceId":"Meter",
     * "serviceData":{
     * "currentReading":"46.3",
     * "signalStrength":16,
     * "dailyActivityTime":5706
     */

    /**
     * Example2:
     * InNlcnZpY2VJZCI6IldhdGVyTWV0ZXIiLA0KImNtZCI6IlNFVF9URU1QRVJBVFVSRV9SRUFEX
     * 1BFUklPRCIsDQoicGFyYXMiOnsNCiJ2YWx1ZSI6NA0KICAgIH0sDQoNCg0K
     * "serviceId":"WaterMeter",
     * "cmd":"SET_TEMPERATURE_READ_PERIOD",
     * "paras":{
     * "value":4
     * },
     */

    Map<Integer, byte[]> data;
    private Integer priority = 0;
    private Time timestamp;
    private String description;
    private String dataFormat;
    private Integer appID = -1;
    private static final List<Integer> supportedResources = Arrays.asList(0, 1, 2, 3, 4, 5);
    private final Timer timer;
    private final Random rng = new Random();

    public LwM2mBinaryAppDataContainer() {
        // notify new date each 5 second
        this(5);
    }

    public LwM2mBinaryAppDataContainer(Integer timeDataFrequency) {
        // notify new date each 5 second
        this.timer = new Timer("Id = [19] BinaryAppDataContainer -> schedule Time period = [" + timeDataFrequency + "] sec");
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                fireResourceChange(0);
                fireResourceChange(2);
            }
        }, timeDataFrequency*1000, timeDataFrequency*1000);
    }

    public LwM2mBinaryAppDataContainer(Integer timeDataFrequency, Integer id) {
        if (id != null) this.setId(id);
        // notify new date each 5 second
        this.timer = new Timer("Id = [19] BinaryAppDataContainer -> schedule Time period = [" + timeDataFrequency + "] sec");
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                fireResourceChange(0);
                fireResourceChange(2);
            }
        }, timeDataFrequency*1000, timeDataFrequency*1000);
    }

    @Override
    public ReadResponse read(LwM2mServer server, int resourceId) {
        try {
            Object value;
            switch (resourceId) {
                case 0:
                    value = getData();
                    printReadLog(server, getModel().name, getModel().id, getId(), resourceId, printMap ((Map<Integer, ?>) value) );
                    return  ReadResponse.success(resourceId, (Map<Integer, ?>) value, ResourceModel.Type.OPAQUE);
                case 1:
                    value = getPriority();
                    printReadLog(server, getModel().name, getModel().id, getId(), resourceId, value);
                    return ReadResponse.success(resourceId, (int) value);
                case 2:
                    value = getTimestamp();
                    printReadLog(server, getModel().name, getModel().id, getId(), resourceId, value);
                    return ReadResponse.success(resourceId, (Time) value);
                case 3:
                    value = getDescription();
                    printReadLog(server, getModel().name, getModel().id, getId(), resourceId, value);
                    return ReadResponse.success(resourceId, (String) value);
                case 4:
                    value = getDataFormat();
                    printReadLog(server, getModel().name, getModel().id, getId(), resourceId, value);
                    return ReadResponse.success(resourceId, (String) value);
                case 5:
                    value = getAppID();
                    printReadLog(server, getModel().name, getModel().id, getId(), resourceId, value);
                    return ReadResponse.success(resourceId, (Integer) value);
                default:
                    return super.read(server, resourceId);
            }
        } catch (Exception e) {
            return ReadResponse.badRequest(e.getMessage());
        }
    }

    @Override
    public WriteResponse write(LwM2mServer identity, boolean replace, int resourceId, LwM2mResource value) {
        log.info("Write on Device resource /[{}]/[{}]/[{}]", getModel().id, getId(), resourceId);
        switch (resourceId) {
            case 0:
                if (setData(value, replace)) {
                    fireResourceChange(resourceId);
                    return WriteResponse.success();
                } else {
                    return WriteResponse.badRequest("Invalidate value ...");
                }
            case 1:
                setPriority((Integer) (value.getValue() instanceof Long ? ((Long) value.getValue()).intValue() : value.getValue()));
                fireResourceChange(resourceId);
                return WriteResponse.success();
            case 2:
                setTimestamp();
                fireResourceChange(resourceId);
                return WriteResponse.success();
            case 3:
                setDescription((String) value.getValue());
                fireResourceChange(resourceId);
                return WriteResponse.success();
            case 4:
                setDataFormat((String) value.getValue());
                fireResourceChange(resourceId);
                return WriteResponse.success();
            case 5:
                setAppID((Integer) value.getValue());
                fireResourceChange(resourceId);
                return WriteResponse.success();
            default:
                return super.write(identity, replace, resourceId, value);
        }
    }

    private Integer getAppID() {
        return this.appID;
    }

    private void setAppID(Integer appId) {
        this.appID = appId;
    }

    private void setDataFormat(String value) {
        this.dataFormat = value;
    }

    private String getDataFormat() {
        return this.dataFormat == null ? "OPAQUE" : this.dataFormat;
    }

    private void setDescription(String value) {
        this.description = value;
    }

    private String getDescription() {
        return this.description;
    }

    private void setTimestamp() {
        long currentTimeMillis = System.currentTimeMillis();
        this.timestamp = new Time(currentTimeMillis);
    }

    private Time getTimestamp() {
        LocalTime localTime = LocalTime.ofInstant(Instant.now(), ZoneId.systemDefault());
        this.timestamp = Time.valueOf(localTime);
        return this.timestamp;
    }

    private boolean setData(LwM2mResource value, boolean replace) {
        try {
            if (value instanceof LwM2mMultipleResource) {
                if (replace || this.data == null) {
                    this.data = new HashMap<>();
                }
                value.getInstances().values().forEach(v -> {
                    this.data.put(v.getId(), (byte[]) v.getValue());
                    if (FW_INFO_19_INSTANCE_ID.equals(this.id) || SW_INFO_19_INSTANCE_ID.equals(this.id)) {
                        String infoNodeStr = new String((byte[]) v.getValue());
                        JsonNode infoNode = Utils.toJsonNode(infoNodeStr);
                        LwM2MClientOtaInfo otaInfo = treeToValue(infoNode, LwM2MClientOtaInfo.class);
                        String fileName = otaInfo.getFileName() == null ? FW_DATA_FILE_NANE_DEF : PREF_FW + otaInfo.getFileName();
                        OtaPackageType otaPackageType = OtaPackageType.FIRMWARE;
                         if (SW_INFO_19_INSTANCE_ID.equals(this.id)) {
                            fileName = otaInfo.getFileName() == null ? SW_DATA_FILE_NANE_DEF : PREF_SW + otaInfo.getFileName();
                            otaPackageType = OtaPackageType.SOFTWARE;
                        }
                        otaInfo.setType(otaPackageType);
                        otaInfo.setFileName(fileName);
                        setOtaInfoUpdate(otaInfo);
                        log.info("otainfo: [{}], value: {}", otaInfo, v.getValue());

                    }
                });
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
    }

    private Map<Integer, byte[]> getData() {
        if (data == null) {
            this.data = new HashMap<>();
            int newData = rng.nextInt(20);
            this.data.put(0, new byte[]{(byte) newData});
        }
        return data;
    }

    @Override
    public List<Integer> getAvailableResourceIds(ObjectModel model) {
        return supportedResources;
    }

    @Override
    public void destroy() {
    }

    private int getPriority() {
        return this.priority;
    }

    private void setPriority(int value) {
        this.priority = value;
    }
}
