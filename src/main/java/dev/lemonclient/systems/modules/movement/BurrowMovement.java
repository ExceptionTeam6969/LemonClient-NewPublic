package dev.lemonclient.systems.modules.movement;

import dev.lemonclient.events.entity.player.PlayerMoveEvent;
import dev.lemonclient.mixininterface.IVec3d;
import dev.lemonclient.settings.DoubleSetting;
import dev.lemonclient.settings.Setting;
import dev.lemonclient.settings.SettingGroup;
import dev.lemonclient.systems.modules.Categories;
import dev.lemonclient.systems.modules.Module;
import dev.lemonclient.systems.modules.Modules;
import dev.lemonclient.utils.entity.EntityInfo;
import dev.lemonclient.utils.player.PlayerUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.math.Vec3d;

public class BurrowMovement extends Module {
    public BurrowMovement() {
        super(Categories.Movement, "Burrow Movement", "Modifies your movement speed when moving in burrow block(s).");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Double> speed = sgGeneral.add(new DoubleSetting.Builder()
        .name("Speed")
        .description("How many blocks to move every movement tick")
        .defaultValue(0.522)
        .min(0)
        .sliderMax(1)
        .build()
    );
    public final Setting<Double> webspeed = sgGeneral.add(new DoubleSetting.Builder()
        .name("Web Speed")
        .description("The speed at which you get stuck in a cobweb.")
        .defaultValue(0.3)
        .range(0, 10)
        .sliderRange(0, 10)
        .build()
    );
    public final Setting<Double> effectspeed = sgGeneral.add(new DoubleSetting.Builder()
        .name("Effect Speed")
        .description("The speed when you gain the speed effect.")
        .defaultValue(0.3)
        .range(0, 10)
        .sliderRange(0, 10)
        .build()
    );
    public double velocity;

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (EntityInfo.isBurrowed(mc.player)) {
            Vec3d vel = PlayerUtils.getHorizontalVelocity(EntityInfo.isWebbed(mc.player) ? webspeed.get() : mc.player.hasStatusEffect(StatusEffects.SPEED) ? effectspeed.get() : speed.get());
            double velX = vel.getX();
            double velZ = vel.getZ();

            Anchor anchor = Modules.get().get(Anchor.class);
            if (anchor.isActive() && anchor.controlMovement) {
                velX = anchor.deltaX;
                velZ = anchor.deltaZ;
            }

            ((IVec3d) event.movement).set(velX, event.movement.y, velZ);
        }
    }
}
