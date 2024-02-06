package dev.lemonclient.systems.modules.player;

import dev.lemonclient.events.packets.PacketEvent;
import dev.lemonclient.mixin.IPlayerPositionLookS2CPacket;
import dev.lemonclient.systems.modules.Categories;
import dev.lemonclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;

public class NoRotate extends Module {
    public NoRotate() {
        super(Categories.Player, "No Rotate", "Attempts to block rotations sent from server to client.");
    }

    @EventHandler
    private void onReceivePacket(PacketEvent.Receive event) {
        if (event.packet instanceof PlayerPositionLookS2CPacket) {
            ((IPlayerPositionLookS2CPacket) event.packet).setPitch(mc.player.getPitch());
            ((IPlayerPositionLookS2CPacket) event.packet).setYaw(mc.player.getYaw());
        }
    }
}
