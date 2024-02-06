package dev.lemonclient.managers.impl;

import dev.lemonclient.LemonClient;
import dev.lemonclient.events.packets.PacketEvent;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

public class OnGroundManager {

    private boolean onGround;

    public OnGroundManager() {
        LemonClient.EVENT_BUS.subscribe(this);
        this.onGround = false;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onPacket(PacketEvent.Send event) {
        if (event.packet instanceof PlayerMoveC2SPacket) {
            onGround = ((PlayerMoveC2SPacket) event.packet).isOnGround();
        }
    }

    public boolean isOnGround() {
        return onGround;
    }
}
