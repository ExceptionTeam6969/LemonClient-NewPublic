package dev.lemonclient.events.rotate;

public class PitchDiffEvent {
    private static final PitchDiffEvent INSTANCE = new PitchDiffEvent();

    public double pitchDiff;

    public static PitchDiffEvent get(double diff) {
        INSTANCE.pitchDiff = diff;
        return INSTANCE;
    }
}
