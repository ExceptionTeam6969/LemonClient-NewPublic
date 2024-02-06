package dev.lemonclient.systems.modules.movement;

import dev.lemonclient.events.entity.player.ClipAtLedgeEvent;
import dev.lemonclient.systems.modules.Categories;
import dev.lemonclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;

public class SafeWalk extends Module {
    public SafeWalk() {
        super(Categories.Movement, "Safe Walk", "Prevents you from walking off blocks.");
    }

    @EventHandler
    private void onClipAtLedge(ClipAtLedgeEvent event) {
        if (!mc.player.isSneaking()) event.setClip(true);
    }
}
