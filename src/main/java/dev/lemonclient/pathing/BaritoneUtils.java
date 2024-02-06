package dev.lemonclient.pathing;

import baritone.api.BaritoneAPI;

public class BaritoneUtils {
    public static boolean IS_AVAILABLE = false;

    public static String getPrefix() {
        if (IS_AVAILABLE) {
            return BaritoneAPI.getSettings().prefix.value;
        }

        return "";
    }
}
