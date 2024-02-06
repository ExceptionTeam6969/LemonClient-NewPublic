package dev.lemonclient.managers.impl;

import dev.lemonclient.LemonClient;
import dev.lemonclient.events.render.Render3DEvent;
import dev.lemonclient.systems.modules.Modules;
import dev.lemonclient.systems.modules.client.PingSpoof;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.KeepAliveC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayPongC2SPacket;

import java.util.ArrayList;
import java.util.List;

import static dev.lemonclient.LemonClient.mc;

public class PingSpoofManager {
    private final List<DelayedPacket> delayed = new ArrayList<>();
    private DelayedPacket delayed1 = null;
    private DelayedPacket delayed2 = null;

    public PingSpoofManager() {
        LemonClient.EVENT_BUS.subscribe(this);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onRender(Render3DEvent event) {
        List<DelayedPacket> toSend = new ArrayList<>();

        if (delayed1 != null) {
            delayed.add(delayed1);
            delayed1 = null;
        }
        if (delayed2 != null) {
            delayed.add(delayed2);
            delayed2 = null;
        }

        for (DelayedPacket d : delayed) {
            if (System.currentTimeMillis() > d.time) toSend.add(d);
        }

        toSend.forEach(d -> {
            mc.getNetworkHandler().sendPacket(d.packet);
            delayed.remove(d);
        });

        toSend.clear();
    }

    public void addKeepAlive(long id) {
        delayed1 = new DelayedPacket(new KeepAliveC2SPacket(id), System.currentTimeMillis() + Modules.get().get(PingSpoof.class).ping.get());
    }

    public void addPong(int id) {
        delayed2 = new DelayedPacket(new PlayPongC2SPacket(id), System.currentTimeMillis() + Modules.get().get(PingSpoof.class).ping.get());
    }

    private record DelayedPacket(Packet<?> packet, long time) {
    }
}
