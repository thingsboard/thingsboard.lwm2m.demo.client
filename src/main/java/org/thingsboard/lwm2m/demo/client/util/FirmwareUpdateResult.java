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
package org.thingsboard.lwm2m.demo.client.util;

public enum FirmwareUpdateResult {
    INITIAL(0, "Initial value"),
    SUCCESS(1, "Firmware updated successfully"),
    NOT_ENOUGH_FLASH(2, "Not enough flash memory for the new firmware package"),
    OUT_OF_RAM(3, "Out of RAM during downloading process"),
    CONNECTION_LOST(4, "Connection lost during downloading process"),
    INTEGRITY_CHECK_FAILURE(5, "Integrity check failure for new downloaded package"),
    UNSUPPORTED_PACKAGE_TYPE(6, "Unsupported package type"),
    INVALID_URI(7, "Invalid URI"),
    FAILED(8, "Firmware update failed"),
    UNSUPPORTED_PROTOCOL(9, "Unsupported protocol"),
    CANCELLED(10, "Firmware update cancelled"),
    DEFERRED(11, "Firmware update deferred");

    private final int code;
    private final String type;

    FirmwareUpdateResult(int code, String type) {
        this.code = code;
        this.type = type;
    }

    public int getCode() {
        return code;
    }

    public String getType() {
        return type;
    }

    public static FirmwareUpdateResult fromCode(int code) {
        for (FirmwareUpdateResult status : FirmwareUpdateResult.values()) {
            if (status.code == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown FirmwareUpdateStatus code: " + code);
    }
}
