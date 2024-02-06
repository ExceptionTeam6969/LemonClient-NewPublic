package dev.lemonclient.systems.modules.movement;

import dev.lemonclient.events.entity.player.JumpVelocityMultiplierEvent;
import dev.lemonclient.settings.DoubleSetting;
import dev.lemonclient.settings.Setting;
import dev.lemonclient.settings.SettingGroup;
import dev.lemonclient.systems.modules.Categories;
import dev.lemonclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;

public class HighJump extends Module {
    public HighJump() {
        super(Categories.Movement, "High Jump", "Makes you jump higher than normal.");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Double> multiplier = sgGeneral.add(new DoubleSetting.Builder()
        .name("jump-multiplier")
        .description("Jump height multiplier.")
        .defaultValue(1)
        .min(0)
        .build()
    );

    @EventHandler
    private void onJumpVelocityMultiplier(JumpVelocityMultiplierEvent event) {
        event.multiplier *= multiplier.get();
    }
}
