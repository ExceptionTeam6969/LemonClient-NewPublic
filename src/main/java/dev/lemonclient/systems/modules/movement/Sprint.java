package dev.lemonclient.systems.modules.movement;

import dev.lemonclient.events.world.TickEvent;
import dev.lemonclient.settings.EnumSetting;
import dev.lemonclient.settings.Setting;
import dev.lemonclient.settings.SettingGroup;
import dev.lemonclient.systems.modules.Categories;
import dev.lemonclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;

public class Sprint extends Module {
    public Sprint() {
        super(Categories.Movement, "Sprint", "Automatically sprints.");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    public enum Mode {
        Strict,
        Rage
    }

    private final Setting<Mode> mode = sgGeneral.add(new EnumSetting.Builder<Mode>()
        .name("speed-mode")
        .description("What mode of sprinting.")
        .defaultValue(Mode.Strict)
        .build()
    );

    @Override
    public void onDeactivate() {
        mc.player.setSprinting(false);
    }

    private void sprint() {
        if (mc.player.getHungerManager().getFoodLevel() <= 6) return;
        mc.player.setSprinting(true);
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        switch (mode.get()) {
            case Strict -> {
                if (mc.player.forwardSpeed > 0) {
                    sprint();
                }
            }
            case Rage -> sprint();
        }
    }
}
