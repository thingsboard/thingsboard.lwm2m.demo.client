package org.thingsboard.lwm2m.demo.client.util;

import org.eclipse.californium.scandium.dtls.MaxFragmentLengthExtension.Length;

public class Utils {

    public static Length fromLength(int length) {
        for (Length l : Length.values()) {
            if (l.length() == length) {
                return l;
            }
        }
        return null;
    }
}
