package org.thingsboard.lwm2m.demo.client.util;

public enum FirmwareUpdateState {
    IDLE(0, "Idle"),
    DOWNLOADING(1, "Downloading"),
    DOWNLOADED(2, "Downloaded"),
    UPDATING(3, "Updating");

    private final int code;
    private final String description;

    FirmwareUpdateState(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static FirmwareUpdateState fromCode(int code) {
        for (FirmwareUpdateState state : FirmwareUpdateState.values()) {
            if (state.code == code) {
                return state;
            }
        }
        throw new IllegalArgumentException("Unknown FirmwareUpdateState code: " + code);
    }
}

