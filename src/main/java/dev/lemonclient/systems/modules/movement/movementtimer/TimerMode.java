package dev.lemonclient.systems.modules.movement.movementtimer;

import dev.lemonclient.events.packets.PacketEvent;
import dev.lemonclient.events.world.TickEvent;
import dev.lemonclient.systems.modules.Modules;

public class TimerMode {
    protected final MovementTimer settings;
    private final TimerModes type;

    public TimerMode(TimerModes type) {
        this.settings = Modules.get().get(MovementTimer.class);
        this.type = type;
    }

    public void onSendPacket(PacketEvent.Send event) {
    }

    public void onSentPacket(PacketEvent.Sent event) {
    }

    public void onPreTick(TickEvent.Pre event) {
    }

    public void onPostTick(TickEvent.Post event) {
    }

    public void onActivate() {
    }

    public void onDeactivate() {
    }
}
