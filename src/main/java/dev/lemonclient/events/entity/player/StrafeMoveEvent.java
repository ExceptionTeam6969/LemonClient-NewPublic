package dev.lemonclient.events.entity.player;

import net.minecraft.util.math.Vec3d;

public class StrafeMoveEvent {
    private static final StrafeMoveEvent INSTANCE = new StrafeMoveEvent();
    public Vec3d movementInput;
    public float speed;
    public float yaw;

    public static StrafeMoveEvent get(Vec3d movementInput, float speed, float yaw) {
        INSTANCE.movementInput = movementInput;
        INSTANCE.speed = speed;
        INSTANCE.yaw = yaw;
        return INSTANCE;
    }
}
