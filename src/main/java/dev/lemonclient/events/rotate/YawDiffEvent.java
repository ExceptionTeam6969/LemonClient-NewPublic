package dev.lemonclient.events.rotate;

public class YawDiffEvent {
    private static final YawDiffEvent INSTANCE = new YawDiffEvent();

    public double yawDiff;

    public static YawDiffEvent get(double diff) {
        INSTANCE.yawDiff = diff;
        return INSTANCE;
    }
}
