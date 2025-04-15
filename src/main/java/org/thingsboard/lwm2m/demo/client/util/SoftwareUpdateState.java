package org.thingsboard.lwm2m.demo.client.util;

import lombok.Getter;



    /**
     * 0: INITIAL
     * Before downloading.
     * (see 5.1.2.1)
     * 1: DOWNLOAD STARTED
     * The downloading process has started and is on-going.
     * (see 5.1.2.2)
     * 2: DOWNLOADED
     * The package has been completely downloaded
     * (see 5.1.2.3)
     * 3: DELIVERED
     * In that state, the package has been correctly downloaded and is ready to be installed. -> verify
     * (see 5.1.2.4)
     * If executing the Install Resource failed, the state remains at DELIVERED.
     * If executing the Install Resource was successful, the state changes from DELIVERED to INSTALLED.
     * After executing the UnInstall Resource, the state changes to INITIAL.
     * 4: INSTALLED
     * In that state the software is correctly installed and can be activated or deactivated according to the Activation State Machine.
     * (see 5.1.2.5)
     */
public enum SoftwareUpdateState {
    INITIAL(0, "Initial"),
    DOWNLOAD_STARTED(1, "DownloadStarted"),
    DOWNLOADED(2, "Downloaded"),
    DELIVERED(3, "Delivered"),
    INSTALLED(4, "Installed");

    @Getter
    private int code;
    @Getter
    private String type;

    SoftwareUpdateState(int code, String type) {
        this.code = code;
        this.type = type;
    }

    public static SoftwareUpdateState fromUpdateStateSwByType(String type) {
        for (SoftwareUpdateState to : SoftwareUpdateState.values()) {
            if (to.type.equals(type)) {
                return to;
            }
        }
        throw new IllegalArgumentException(String.format("Unsupported SW State type  : %s", type));
    }

    public static SoftwareUpdateState fromUpdateStateSwByCode(int code) {
        for (SoftwareUpdateState to : SoftwareUpdateState.values()) {
            if (to.code == code) {
                return to;
            }
        }
        throw new IllegalArgumentException(String.format("Unsupported SW State type  : %s", code));
    }
}

