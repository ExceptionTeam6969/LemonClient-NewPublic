package dev.lemonclient.events.entity.player;

public class SprintEvent {
    private static final SprintEvent INSTANCE = new SprintEvent();

    public boolean isSprint;

    public static SprintEvent get(boolean isSprint) {
        INSTANCE.isSprint = isSprint;
        return INSTANCE;
    }
}
