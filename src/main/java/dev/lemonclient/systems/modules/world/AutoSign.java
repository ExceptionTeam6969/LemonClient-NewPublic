package dev.lemonclient.systems.modules.world;

import dev.lemonclient.events.game.OpenScreenEvent;
import dev.lemonclient.events.packets.PacketEvent;
import dev.lemonclient.mixin.IAbstractSignEditScreen;
import dev.lemonclient.systems.modules.Categories;
import dev.lemonclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.client.gui.screen.ingame.AbstractSignEditScreen;
import net.minecraft.network.packet.c2s.play.UpdateSignC2SPacket;

public class AutoSign extends Module {
    public AutoSign() {
        super(Categories.World, "Auto Sign", "Automatically writes signs. The first sign's text will be used.");
    }

    private String[] text;

    @Override
    public void onDeactivate() {
        text = null;
    }

    @EventHandler
    private void onSendPacket(PacketEvent.Send event) {
        if (!(event.packet instanceof UpdateSignC2SPacket)) return;

        text = ((UpdateSignC2SPacket) event.packet).getText();
    }

    @EventHandler
    private void onOpenScreen(OpenScreenEvent event) {
        if (!(event.screen instanceof AbstractSignEditScreen) || text == null) return;

        SignBlockEntity sign = ((IAbstractSignEditScreen) event.screen).getSign();

        mc.player.networkHandler.sendPacket(new UpdateSignC2SPacket(sign.getPos(), true, text[0], text[1], text[2], text[3]));

        event.cancel();
    }
}
