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
import org.eclipse.leshan.core.request.argument.Arguments;
import org.eclipse.leshan.core.response.ExecuteResponse;
import org.eclipse.leshan.core.response.ReadResponse;
import org.eclipse.leshan.core.util.NamedThreadFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.thingsboard.lwm2m.demo.client.util.Utils.printReadLog;

@Slf4j
public class RandomTemperatureSensor extends BaseInstanceEnabler implements Destroyable {

    private static final String UNIT_CELSIUS = "cel";
    private static final int SENSOR_VALUE = 5700;
    private static final int UNITS = 5701;
    private static final int MAX_MEASURED_VALUE = 5602;
    private static final int MIN_MEASURED_VALUE = 5601;
    private static final int RESET_MIN_MAX_MEASURED_VALUES = 5605;
    private static final List<Integer> supportedResources = Arrays.asList(SENSOR_VALUE, UNITS, MAX_MEASURED_VALUE,
            MIN_MEASURED_VALUE, RESET_MIN_MAX_MEASURED_VALUES);
    private final ScheduledExecutorService scheduler;
    private final Random rng = new Random();
    private double currentTemp = 20d;
    private double minMeasuredValue = currentTemp;
    private double maxMeasuredValue = currentTemp;

    public RandomTemperatureSensor() {
        this(5);
    }

    public RandomTemperatureSensor(Integer timeDataFrequency) {
        this.scheduler = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("Id = 3303 - TemperatureSensor -> schedule Time period = [" + timeDataFrequency + "] sec"));
        scheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                adjustTemperature();
            }
        }, timeDataFrequency, timeDataFrequency, TimeUnit.SECONDS);
    }

    @Override
    public synchronized ReadResponse read(LwM2mServer server, int resourceId) {
        Object value;
        return switch (resourceId) {
            case MIN_MEASURED_VALUE -> {
                value = getTwoDigitValue(minMeasuredValue);
                printReadLog(server, getModel().name, getModel().id, getId(), resourceId, value);
                yield ReadResponse.success(resourceId, (double)value);
            }
            case MAX_MEASURED_VALUE -> {
                value = getTwoDigitValue(maxMeasuredValue);
                printReadLog(server, getModel().name, getModel().id, getId(), resourceId, value);
                yield ReadResponse.success(resourceId, (double)value);
            }
            case SENSOR_VALUE -> {
                value = getTwoDigitValue(currentTemp);
                printReadLog(server, getModel().name, getModel().id, getId(), resourceId, getTwoDigitValue(currentTemp));
                yield ReadResponse.success(resourceId, (double)value);
            }
            case UNITS -> {
                printReadLog(server, getModel().name, getModel().id, getId(), resourceId, UNIT_CELSIUS);
                yield ReadResponse.success(resourceId, UNIT_CELSIUS);
            }
            default -> super.read(server, resourceId);
        };
    }

    @Override
    public synchronized ExecuteResponse execute(LwM2mServer server, int resourceId, Arguments arguments) {
        log.info("Execute on Temperature resource /{}/{}/{}", getModel().id, getId(), resourceId);
        switch (resourceId) {
        case RESET_MIN_MAX_MEASURED_VALUES:
            resetMinMaxMeasuredValues();
            return ExecuteResponse.success();
        default:
            return super.execute(server, resourceId, arguments);
        }
    }

//    private void printReadLog (int modelId, int resourceId, Object value, String nameClazz) {
//        log.info("Read on Temperature resource /{}/{}/{} = {}", modelId, getId(), resourceId, value);
//    }

    private double getTwoDigitValue(double value) {
        BigDecimal toBeTruncated = BigDecimal.valueOf(value);
        return toBeTruncated.setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    private void adjustTemperature() {
        float delta = (rng.nextInt(20) - 10) / 10f;
        currentTemp += delta;
        Integer changedResource = adjustMinMaxMeasuredValue(currentTemp);
        if (changedResource != null) {
            fireResourcesChange(getResourcePath(SENSOR_VALUE), getResourcePath(changedResource));
        } else {
            fireResourceChange(SENSOR_VALUE);
        }
    }

    private synchronized Integer adjustMinMaxMeasuredValue(double newTemperature) {
        if (newTemperature > maxMeasuredValue) {
            maxMeasuredValue = newTemperature;
            return MAX_MEASURED_VALUE;
        } else if (newTemperature < minMeasuredValue) {
            minMeasuredValue = newTemperature;
            return MIN_MEASURED_VALUE;
        } else {
            return null;
        }
    }

    private void resetMinMaxMeasuredValues() {
        minMeasuredValue = currentTemp;
        maxMeasuredValue = currentTemp;
    }

    @Override
    public List<Integer> getAvailableResourceIds(ObjectModel model) {
        return supportedResources;
    }

    @Override
    public void destroy() {
        scheduler.shutdown();
    }
}
