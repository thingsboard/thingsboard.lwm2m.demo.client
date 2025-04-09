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
    private final String description;

    FirmwareUpdateResult(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
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
