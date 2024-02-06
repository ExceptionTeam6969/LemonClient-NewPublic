package dev.lemonclient.utils.player;

import dev.lemonclient.LemonClient;
import dev.lemonclient.events.packets.PacketEvent;
import dev.lemonclient.systems.modules.Module;
import dev.lemonclient.systems.modules.Modules;
import dev.lemonclient.systems.modules.chat.AutoEz;
import dev.lemonclient.utils.PreInit;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;

import java.util.ArrayList;

import static dev.lemonclient.LemonClient.mc;

public class DeathUtils {
    private static final int DeathStatus = 3;

    @PreInit
    public static void init() {
        LemonClient.EVENT_BUS.subscribe(DeathUtils.class);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private static void onReceivePacket(PacketEvent.Receive event) {
        if (getTargets().isEmpty()) return;

        if (!(event.packet instanceof EntityStatusS2CPacket packet)) return;
        if (packet.getStatus() != DeathStatus) return;

        Entity entity = packet.getEntity(mc.world);
        if (entity == null) return;

        if (entity instanceof PlayerEntity player && getTargets().contains(player.getGameProfile().getName())) {
            Modules.get().get(AutoEz.class).onKill(player);
        }
    }

    public static ArrayList<String> getTargets() {
        ArrayList<String> list = new ArrayList<>();

        for (Module module : Modules.get().getAll()) {
            String name = module.getInfoString();

            if (module.isActive() && name != null && !list.contains(name)) list.add(name);
        }

        try {
            list.removeIf(name -> !isName(name));
        } catch (Exception exception) {
            exception.fillInStackTrace();
        }
        return list;
    }

    private static boolean isName(String string) {
        ArrayList<PlayerListEntry> playerListEntries = new ArrayList<>(mc.getNetworkHandler().getPlayerList());

        for (PlayerListEntry entry : playerListEntries) {
            if (string.contains(entry.getProfile().getName())) return true;
        }
        return false;
    }
}
