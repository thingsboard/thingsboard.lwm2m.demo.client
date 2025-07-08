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

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.leshan.client.resource.BaseInstanceEnabler;
import org.eclipse.leshan.client.servers.LwM2mServer;
import org.eclipse.leshan.core.model.ObjectModel;
import org.eclipse.leshan.core.response.ReadResponse;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import static org.thingsboard.lwm2m.demo.client.util.Utils.printReadLog;

@Slf4j
public class MyLocation extends BaseInstanceEnabler {

    private static final List<Integer> supportedResources = Arrays.asList(0, 1, 5);
    private static final Random RANDOM = new Random();

    private float latitude;
    private float longitude;
    private final float scaleFactor;
    @Getter
    private Date timestamp;
    private final Timer timer;

    public MyLocation() {
        this(5, null, null, 1.0f);
    }

    public MyLocation(Integer timeDataFrequency, Float latitude, Float longitude, float scaleFactor) {
        if (latitude != null) {
            this.latitude = latitude + 90f;
        } else {
            this.latitude = RANDOM.nextInt(180);
        }
        if (longitude != null) {
            this.longitude = longitude + 180f;
        } else {
            this.longitude = RANDOM.nextInt(360);
        }
        this.scaleFactor = scaleFactor;
        this.timestamp = new Date();
        this.timer = new Timer("Id = [3] Device -> schedule Time period = [" + timeDataFrequency + "] sec");
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                fireResourceChange(0);
                fireResourceChange(1);
                fireResourceChange(5);
            }
        }, timeDataFrequency*1000, timeDataFrequency*1000);
    }

    @Override
    public ReadResponse read(LwM2mServer server, int resourceId) {
        Object value;
        switch (resourceId) {
        case 0:
            value = getLatitude();
            printReadLog(server, getModel().name, getModel().id, getId(), resourceId, value);
            return ReadResponse.success(resourceId, (float) value);
        case 1:
            value = getLongitude();
            printReadLog(server, getModel().name, getModel().id, getId(), resourceId, value);
            return ReadResponse.success(resourceId, (float) value);
        case 5:
            value = getTimestamp();
            printReadLog(server, getModel().name, getModel().id, getId(), resourceId, value);
            return ReadResponse.success(resourceId, (Date) value);
        default:
            return super.read(server, resourceId);
        }
    }

    public void moveLocation(String nextMove) {
        switch (nextMove.charAt(0)) {
        case 'd':
            moveLatitude(1.0f);
            log.info("Move to North {}/{}", getLatitude(), getLongitude());
            break;
        case 'e':
            moveLongitude(-1.0f);
            log.info("Move to East {}/{}", getLatitude(), getLongitude());
            break;
        case 's':
            moveLatitude(-1.0f);
            log.info("Move to South {}/{}", getLatitude(), getLongitude());
            break;
        case 'w':
            moveLongitude(1.0f);
            log.info("Move to West {}/{}", getLatitude(), getLongitude());
            break;
        }
    }

    private void moveLatitude(float delta) {
        this.latitude = this.latitude + delta * this.scaleFactor;
        this.timestamp = new Date();
        fireResourcesChange(getResourcePath(0), getResourcePath(5));
    }

    private void moveLongitude(float delta) {
        this.longitude = this.longitude + delta * this.scaleFactor;
        this.timestamp = new Date();
        fireResourcesChange(getResourcePath(1), getResourcePath(5));
    }

    public float getLatitude() {
        return this.latitude - 90.0f;
    }

    public float getLongitude() {
        return this.longitude - 180.f;
    }

    @Override
    public List<Integer> getAvailableResourceIds(ObjectModel model) {
        return supportedResources;
    }
}
