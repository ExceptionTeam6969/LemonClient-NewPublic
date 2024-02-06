package dev.lemonclient.systems.modules.movement;

import dev.lemonclient.events.entity.LivingEntityMoveEvent;
import dev.lemonclient.mixininterface.IVec3d;
import dev.lemonclient.settings.BoolSetting;
import dev.lemonclient.settings.DoubleSetting;
import dev.lemonclient.settings.Setting;
import dev.lemonclient.settings.SettingGroup;
import dev.lemonclient.systems.modules.Categories;
import dev.lemonclient.systems.modules.Module;
import dev.lemonclient.utils.player.PlayerUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;

public class EntitySpeed extends Module {
    public EntitySpeed() {
        super(Categories.Movement, "Entity Speed", "Makes you go faster when riding entities.");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Double> speed = sgGeneral.add(new DoubleSetting.Builder()
        .name("speed")
        .description("Horizontal speed in blocks per second.")
        .defaultValue(10)
        .min(0)
        .sliderMax(50)
        .build()
    );

    private final Setting<Boolean> onlyOnGround = sgGeneral.add(new BoolSetting.Builder()
        .name("only-on-ground")
        .description("Use speed only when standing on a block.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> inWater = sgGeneral.add(new BoolSetting.Builder()
        .name("in-water")
        .description("Use speed when in water.")
        .defaultValue(false)
        .build()
    );

    @EventHandler
    private void onLivingEntityMove(LivingEntityMoveEvent event) {
        if (event.entity.getControllingPassenger() != mc.player) return;

        // Check for onlyOnGround and inWater
        LivingEntity entity = event.entity;
        if (onlyOnGround.get() && !entity.isOnGround()) return;
        if (!inWater.get() && entity.isTouchingWater()) return;

        // Set horizontal velocity
        Vec3d vel = PlayerUtils.getHorizontalVelocity(speed.get());
        ((IVec3d) event.movement).setXZ(vel.x, vel.z);
    }
}
