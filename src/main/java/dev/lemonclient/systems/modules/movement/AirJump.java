package dev.lemonclient.systems.modules.movement;

import dev.lemonclient.events.client.KeyEvent;
import dev.lemonclient.events.world.TickEvent;
import dev.lemonclient.settings.BoolSetting;
import dev.lemonclient.settings.Setting;
import dev.lemonclient.settings.SettingGroup;
import dev.lemonclient.systems.modules.Categories;
import dev.lemonclient.systems.modules.Module;
import dev.lemonclient.systems.modules.Modules;
import dev.lemonclient.systems.modules.render.Freecam;
import dev.lemonclient.utils.misc.input.KeyAction;
import meteordevelopment.orbit.EventHandler;

public class AirJump extends Module {
    public AirJump() {
        super(Categories.Movement, "Air Jump", "Lets you jump in the air.");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> maintainLevel = sgGeneral.add(new BoolSetting.Builder()
        .name("maintain-level")
        .description("Maintains your current Y level when holding the jump key.")
        .defaultValue(false)
        .build()
    );

    private int level;

    @Override
    public void onActivate() {
        level = mc.player.getBlockPos().getY();
    }

    @EventHandler
    private void onKey(KeyEvent event) {
        if (Modules.get().isActive(Freecam.class) || mc.currentScreen != null || mc.player.isOnGround()) return;

        if (event.action != KeyAction.Press) return;

        if (mc.options.jumpKey.matchesKey(event.key, 0)) {
            level = mc.player.getBlockPos().getY();
            mc.player.jump();
        } else if (mc.options.sneakKey.matchesKey(event.key, 0)) {
            level--;
        }
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (Modules.get().isActive(Freecam.class) || mc.player.isOnGround()) return;

        if (maintainLevel.get() && mc.player.getBlockPos().getY() == level && mc.options.jumpKey.isPressed()) {
            mc.player.jump();
        }
    }
}
