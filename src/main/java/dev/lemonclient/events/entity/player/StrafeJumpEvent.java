package dev.lemonclient.events.entity.player;

public class StrafeJumpEvent {
    private static final StrafeJumpEvent INSTANCE = new StrafeJumpEvent();
    public float yaw;
    public boolean sprint;

    public static StrafeJumpEvent get(boolean sprint, float yaw) {
        INSTANCE.sprint = sprint;
        INSTANCE.yaw = yaw;
        return INSTANCE;
    }
}
