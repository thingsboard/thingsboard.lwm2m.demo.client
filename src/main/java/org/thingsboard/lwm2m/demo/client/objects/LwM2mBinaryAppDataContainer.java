/**
 * Copyright © 2016-2024 The Thingsboard Authors
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
import org.eclipse.leshan.client.resource.BaseInstanceEnabler;
import org.eclipse.leshan.client.servers.LwM2mServer;
import org.eclipse.leshan.core.model.ObjectModel;
import org.eclipse.leshan.core.model.ResourceModel;
import org.eclipse.leshan.core.node.LwM2mMultipleResource;
import org.eclipse.leshan.core.node.LwM2mResource;
import org.eclipse.leshan.core.response.ReadResponse;
import org.eclipse.leshan.core.response.WriteResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thingsboard.lwm2m.demo.client.entities.LwM2MClientOtaInfo;
import org.thingsboard.lwm2m.demo.client.entities.OtaPackageType;
import org.thingsboard.lwm2m.demo.client.util.Utils;

import javax.security.auth.Destroyable;
import java.sql.Time;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import static org.thingsboard.lwm2m.demo.client.util.Utils.*;


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

    private static final Logger LOG = LoggerFactory.getLogger(MyDevice.class);

    Map<Integer, byte[]> data;
    private Integer priority = 0;
    private Time timestamp;
    private String description;
    private String dataFormat;
    private Integer appID = -1;
    private static final List<Integer> supportedResources = Arrays.asList(0, 1, 2, 3, 4, 5);
    private final Timer timer;
    private boolean objectForTest;

    public LwM2mBinaryAppDataContainer() {
        // notify new date each 5 second
        this.timer = new Timer("19 - Device-Current Time");
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                fireResourceChange(0);
                fireResourceChange(2);
            }
        }, 5000, 5000);
    }

    public LwM2mBinaryAppDataContainer(Integer id, boolean objectForTest) {
        this.objectForTest = objectForTest;
        if (id != null) this.setId(id);
        // notify new date each 5 second
        this.timer = new Timer("19 - Device-Current Time");
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                fireResourceChange(0);
                fireResourceChange(2);
            }
        }, 5000, 5000);

    }

    @Override
    public ReadResponse read(LwM2mServer identity, int resourceId) {
        try {
            switch (resourceId) {
                case 0:
                    ReadResponse response = ReadResponse.success(resourceId, getData(), ResourceModel.Type.OPAQUE);
                    return response;
                case 1:
                    return ReadResponse.success(resourceId, getPriority());
                case 2:
                    return ReadResponse.success(resourceId, getTimestamp());
                case 3:
                    return ReadResponse.success(resourceId, getDescription());
                case 4:
                    return ReadResponse.success(resourceId, getDataFormat());
                case 5:
                    return ReadResponse.success(resourceId, getAppID());
                default:
                    return super.read(identity, resourceId);
            }
        } catch (Exception e) {
            return ReadResponse.badRequest(e.getMessage());
        }
    }

    @Override
    public WriteResponse write(LwM2mServer identity, boolean replace, int resourceId, LwM2mResource value) {
        LOG.info("Write on Device resource /[{}]/[{}]/[{}]", getModel().id, getId(), resourceId);
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
                    if (FW_INFO_19_INSTANCE_ID.equals(this.id)){
                        String infoNodeStr = new String((byte[]) v.getValue());
                        JsonNode infoNode = Utils.toJsonNode(infoNodeStr);
                        LwM2MClientOtaInfo otaInfo = treeToValue(infoNode, LwM2MClientOtaInfo.class);
                        otaInfo.setPackageType(OtaPackageType.FIRMWARE);
                        String fileName = otaInfo.getFileName() == null ? FW_DATA_FILE_NANE_DEF : "FW_" + otaInfo.getFileName();
                        otaInfo.setFileName(fileName);
                        setOtaInfoUpdateFw(otaInfo);
                        LOG.info("otainfo: [{}], value: {}", getOtaInfoUpdateFw(), v.getValue());
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
            this.data.put(0, new byte[]{(byte) 0xAC});
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
