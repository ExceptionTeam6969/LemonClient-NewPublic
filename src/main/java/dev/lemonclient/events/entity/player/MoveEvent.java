package dev.lemonclient.events.entity.player;

import dev.lemonclient.events.Cancellable;
import net.minecraft.entity.MovementType;

public class MoveEvent extends Cancellable {
    private static final MoveEvent INSTANCE = new MoveEvent();

    public MovementType type;
    public double x, y, z;

    public static MoveEvent get(MovementType type, double x, double y, double z) {
        INSTANCE.setCancelled(false);

        INSTANCE.type = type;
        INSTANCE.x = x;
        INSTANCE.y = y;
        INSTANCE.z = z;
        return INSTANCE;
    }
}
