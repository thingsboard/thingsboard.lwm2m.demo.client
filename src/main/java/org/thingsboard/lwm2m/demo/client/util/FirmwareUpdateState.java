package org.thingsboard.lwm2m.demo.client.util;

public enum FirmwareUpdateState {
    IDLE(0, "Idle"),
    DOWNLOADING(1, "Downloading"),
    DOWNLOADED(2, "Downloaded"),
    UPDATING(3, "Updating");

    private final int code;
    private final String type;

    FirmwareUpdateState(int code, String type) {
        this.code = code;
        this.type = type;
    }

    public int getCode() {
        return code;
    }

    public String getType() {
        return type;
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

