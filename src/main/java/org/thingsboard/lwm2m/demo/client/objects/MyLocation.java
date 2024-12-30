/*******************************************************************************
 * Copyright (c) 2022    Sierra Wireless and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * and Eclipse Distribution License v1.0 which accompany this distribution.
 *
 * The Eclipse Public License is available at
 *    http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *    http://www.eclipse.org/org/documents/edl-v10.html.
 *
 * Contributors:
 *     Sierra Wireless - initial API and implementation
 *******************************************************************************/
package org.thingsboard.lwm2m.demo.client.objects;

import org.eclipse.leshan.client.resource.BaseInstanceEnabler;
import org.eclipse.leshan.client.servers.LwM2mServer;
import org.eclipse.leshan.core.model.ObjectModel;
import org.eclipse.leshan.core.response.ReadResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class MyLocation extends BaseInstanceEnabler {

    private static final Logger LOG = LoggerFactory.getLogger(MyLocation.class);

    private static final List<Integer> supportedResources = Arrays.asList(0, 1, 5);
    private static final Random RANDOM = new Random();

    private float latitude;
    private float longitude;
    private final float scaleFactor;
    private Date timestamp;

    public MyLocation() {
        this(null, null, 1.0f);
    }

    public MyLocation(Float latitude, Float longitude, float scaleFactor) {
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
        timestamp = new Date();
    }

    @Override
    public ReadResponse read(LwM2mServer server, int resourceid) {
        LOG.info("Read on Location resource /{}/{}/{}", getModel().id, getId(), resourceid);
        switch (resourceid) {
        case 0:
            return ReadResponse.success(resourceid, getLatitude());
        case 1:
            return ReadResponse.success(resourceid, getLongitude());
        case 5:
            return ReadResponse.success(resourceid, getTimestamp());
        default:
            return super.read(server, resourceid);
        }
    }

    public void moveLocation(String nextMove) {
        switch (nextMove.charAt(0)) {
        case 'w':
            moveLatitude(1.0f);
            LOG.info("Move to North {}/{}", getLatitude(), getLongitude());
            break;
        case 'a':
            moveLongitude(-1.0f);
            LOG.info("Move to East {}/{}", getLatitude(), getLongitude());
            break;
        case 's':
            moveLatitude(-1.0f);
            LOG.info("Move to South {}/{}", getLatitude(), getLongitude());
            break;
        case 'd':
            moveLongitude(1.0f);
            LOG.info("Move to West {}/{}", getLatitude(), getLongitude());
            break;
        }
    }

    private void moveLatitude(float delta) {
        latitude = latitude + delta * scaleFactor;
        timestamp = new Date();
        fireResourcesChange(getResourcePath(0), getResourcePath(5));
    }

    private void moveLongitude(float delta) {
        longitude = longitude + delta * scaleFactor;
        timestamp = new Date();
        fireResourcesChange(getResourcePath(1), getResourcePath(5));
    }

    public float getLatitude() {
        return latitude - 90.0f;
    }

    public float getLongitude() {
        return longitude - 180.f;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    @Override
    public List<Integer> getAvailableResourceIds(ObjectModel model) {
        return supportedResources;
    }
}
