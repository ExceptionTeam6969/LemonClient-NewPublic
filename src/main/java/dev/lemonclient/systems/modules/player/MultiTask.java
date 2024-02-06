package dev.lemonclient.systems.modules.player;

import dev.lemonclient.events.entity.player.InteractEvent;
import dev.lemonclient.systems.modules.Categories;
import dev.lemonclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;

public class MultiTask extends Module {
    public MultiTask() {
        super(Categories.Player, "Multi Task", "Allows you to eat while mining a block.");
    }

    @EventHandler
    public void onInteract(InteractEvent event) {
        event.usingItem = false;
    }
}
