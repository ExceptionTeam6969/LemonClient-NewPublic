package dev.lemonclient.systems.modules.player;

import dev.lemonclient.events.packets.PacketEvent;
import dev.lemonclient.events.world.TickEvent;
import dev.lemonclient.systems.modules.Categories;
import dev.lemonclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.item.Item;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;

public class PacketEat extends Module {
    public PacketEat() {
        super(Categories.Player, "Packet Eat", "Enhanced eating control.");
    }

    private Item packetEatItem;

    @EventHandler
    public void onTick(TickEvent.Post event) {
        if (mc.player.isUsingItem()) {
            packetEatItem = mc.player.getActiveItem().getItem();
        }
    }

    @EventHandler
    public void onPacket(PacketEvent.Send event) {
        if (event.packet instanceof PlayerActionC2SPacket packet) {
            try {
                if (packet.getAction() == PlayerActionC2SPacket.Action.RELEASE_USE_ITEM && this.packetEatItem.getFoodComponent().isAlwaysEdible()) {
                    event.cancel();
                }
            } catch (Exception ignored) {
            }
        }
    }
}
