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
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class SwLwM2MDevice extends BaseInstanceEnabler implements Destroyable {

    private static final Logger LOG = LoggerFactory.getLogger(MyDevice.class);
    private static final List<Integer> supportedResources = Arrays.asList(0, 1, 2, 3, 4, 6, 7, 9);

    private final ScheduledExecutorService scheduler = new ScheduledThreadPoolExecutor(1,
            new DaemonThreadFactory(getClass().getSimpleName() + "-test-scope"));
    private final AtomicInteger state = new AtomicInteger(0);

    private final AtomicInteger updateResult = new AtomicInteger(0);

    private boolean objectForTest;
    private final Timer timer;

    public SwLwM2MDevice() {
        this.timer = new Timer("9 - Device-Current Time");
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                fireResourceChange(7);
            }
        }, 5000, 5000);
    }

    public SwLwM2MDevice(boolean objectForTest) {
        this.objectForTest = objectForTest;
        // notify new date each 5 second
        this.timer = new Timer("9 - Device-Current Time");
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                fireResourceChange(7);
            }
        }, 5000, 5000);
    }

    @Override
    public ReadResponse read(LwM2mServer identity, int resourceId) {
        if (!identity.isSystem())
            LOG.info("Read on Device resource /{}/{}/{}", getModel().id, getId(), resourceId);
        switch (resourceId) {
            case 0:
                return ReadResponse.success(resourceId, getPkgName());
            case 1:
                return ReadResponse.success(resourceId, getPkgVersion());
            case 7:
                return ReadResponse.success(resourceId, getUpdateState());
            case 9:
                return ReadResponse.success(resourceId, getUpdateResult());
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
            case 4:
                if (this.objectForTest) {
                    startUpdating();
                }
                return ExecuteResponse.success();
            case 6:
                return ExecuteResponse.success();
            default:
                return super.execute(identity, resourceId, arguments);
        }
    }

    @Override
    public WriteResponse write(LwM2mServer identity, boolean replace, int resourceId, LwM2mResource value) {
        LOG.info("Write on Device resource /{}/{}/{}", getModel().id, getId(), resourceId);

        switch (resourceId) {
            case 2:
            case 3:
                if(this.objectForTest) {
                    startDownloading();
                }
                return WriteResponse.success();
            default:
                return super.write(identity, replace, resourceId, value);
        }
    }

    private int getUpdateState() {
        return state.get();
    }

    private int getUpdateResult() {
        return updateResult.get();
    }

    private String getPkgName() {
        return "software";
    }

    private String getPkgVersion() {
        return "1.0.0";
    }

    @Override
    public List<Integer> getAvailableResourceIds(ObjectModel model) {
        return supportedResources;
    }

    @Override
    public void destroy() {
        scheduler.shutdownNow();
    }

    private void startDownloading() {
        scheduler.schedule(() -> {
            try {
                state.set(1);
                updateResult.set(1);
                fireResourceChange(7);
                fireResourceChange(9);
                Thread.sleep(100);
                state.set(2);
                fireResourceChange(7);
                Thread.sleep(100);
                state.set(3);
                fireResourceChange(7);
                Thread.sleep(100);
                updateResult.set(3);
                fireResourceChange(9);
            } catch (Exception e) {

            }
        }, 100, TimeUnit.MILLISECONDS);
    }

    private void startUpdating() {
        scheduler.schedule(() -> {
            state.set(4);
            updateResult.set(2);
            fireResourceChange(7);
            fireResourceChange(9);
        }, 100, TimeUnit.MILLISECONDS);
    }

}
