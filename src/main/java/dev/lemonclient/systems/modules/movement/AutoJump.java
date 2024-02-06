package dev.lemonclient.systems.modules.movement;

import dev.lemonclient.events.world.TickEvent;
import dev.lemonclient.mixininterface.IVec3d;
import dev.lemonclient.settings.DoubleSetting;
import dev.lemonclient.settings.EnumSetting;
import dev.lemonclient.settings.Setting;
import dev.lemonclient.settings.SettingGroup;
import dev.lemonclient.systems.modules.Categories;
import dev.lemonclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;

public class AutoJump extends Module {
    public AutoJump() {
        super(Categories.Movement, "Auto Jump", "Automatically jumps.");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Mode> mode = sgGeneral.add(new EnumSetting.Builder<Mode>()
        .name("mode")
        .description("The method of jumping.")
        .defaultValue(Mode.Jump)
        .build()
    );

    private final Setting<JumpWhen> jumpIf = sgGeneral.add(new EnumSetting.Builder<JumpWhen>()
        .name("jump-if")
        .description("Jump if.")
        .defaultValue(JumpWhen.Always)
        .build()
    );

    private final Setting<Double> velocityHeight = sgGeneral.add(new DoubleSetting.Builder()
        .name("velocity-height")
        .description("The distance that velocity mode moves you.")
        .defaultValue(0.25)
        .min(0)
        .sliderMax(2)
        .build()
    );

    private boolean jump() {
        return switch (jumpIf.get()) {
            case Sprinting -> mc.player.isSprinting() && (mc.player.forwardSpeed != 0 || mc.player.sidewaysSpeed != 0);
            case Walking -> mc.player.forwardSpeed != 0 || mc.player.sidewaysSpeed != 0;
            case Always -> true;
        };
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (!mc.player.isOnGround() || mc.player.isSneaking() || !jump()) return;

        if (mode.get() == Mode.Jump) mc.player.jump();
        else ((IVec3d) mc.player.getVelocity()).setY(velocityHeight.get());
    }

    public enum JumpWhen {
        Sprinting,
        Walking,
        Always
    }

    public enum Mode {
        Jump,
        LowHop
    }
}
