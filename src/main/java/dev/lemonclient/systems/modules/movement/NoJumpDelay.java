package dev.lemonclient.systems.modules.movement;

import dev.lemonclient.events.world.TickEvent;
import dev.lemonclient.mixin.ILivingEntity;
import dev.lemonclient.systems.modules.Categories;
import dev.lemonclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;

public class NoJumpDelay extends Module {
    public NoJumpDelay() {
        super(Categories.Movement, "No Jump Delay", "Remove the interval between each jump of the player.");
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        ((ILivingEntity) mc.player).setJumpCooldown(0);
    }
}
