package dev.lemonclient.systems.modules.world;

import dev.lemonclient.events.packets.PacketEvent;
import dev.lemonclient.mixin.IBlockHitResult;
import dev.lemonclient.systems.modules.Categories;
import dev.lemonclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.util.math.Direction;

public class BuildHeight extends Module {
    public BuildHeight() {
        super(Categories.World, "Build Height", "Allows you to interact with objects at the build limit.");
    }

    @EventHandler
    private void onSendPacket(PacketEvent.Send event) {
        if (!(event.packet instanceof PlayerInteractBlockC2SPacket p)) return;
        if (mc.world == null) return;
        if (p.getBlockHitResult().getPos().y >= mc.world.getTopY() && p.getBlockHitResult().getSide() == Direction.UP) {
            ((IBlockHitResult) p.getBlockHitResult()).setSide(Direction.DOWN);
        }
    }
}
