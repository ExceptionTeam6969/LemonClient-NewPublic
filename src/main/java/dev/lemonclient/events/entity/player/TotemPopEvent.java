package dev.lemonclient.events.entity.player;

import net.minecraft.entity.player.PlayerEntity;

public class TotemPopEvent {
    private static final TotemPopEvent INSTANCE = new TotemPopEvent();
    public PlayerEntity entity;
    public int pops;

    public static TotemPopEvent get(PlayerEntity entity, int pops) {
        INSTANCE.entity = entity;
        INSTANCE.pops = pops;
        return INSTANCE;
    }
}
