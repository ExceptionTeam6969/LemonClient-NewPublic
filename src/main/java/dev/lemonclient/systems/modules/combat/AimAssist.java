package dev.lemonclient.systems.modules.combat;

import dev.lemonclient.events.render.Render3DEvent;
import dev.lemonclient.events.world.TickEvent;
import dev.lemonclient.settings.*;
import dev.lemonclient.systems.friends.Friends;
import dev.lemonclient.systems.modules.Categories;
import dev.lemonclient.systems.modules.Module;
import dev.lemonclient.utils.SettingUtils;
import dev.lemonclient.utils.Utils;
import dev.lemonclient.utils.entity.EntityUtils;
import dev.lemonclient.utils.entity.SortPriority;
import dev.lemonclient.utils.entity.Target;
import dev.lemonclient.utils.entity.TargetUtils;
import dev.lemonclient.utils.player.PlayerUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import org.joml.Vector3d;

import java.util.Set;

public class AimAssist extends Module {
    public AimAssist() {
        super(Categories.Combat, "Aim Assist", "Automatically aims at entities.");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgSpeed = settings.createGroup("Aim Speed");

    //--------------------General--------------------//
    private final Setting<Set<EntityType<?>>> entities = sgGeneral.add(new EntityTypeListSetting.Builder()
        .name("entities")
        .description("Entities to aim at.")
        .defaultValue(EntityType.PLAYER)
        .build()
    );
    private final Setting<Boolean> checkFov = sgGeneral.add(new BoolSetting.Builder()
        .name("Check Fov")
        .description("only aim entities in customized fov.")
        .defaultValue(true)
        .build()
    );
    private final Setting<Double> fov = sgGeneral.add(new DoubleSetting.Builder()
        .name("fov")
        .description("Will only aim entities in the fov.")
        .defaultValue(360)
        .min(0)
        .max(360)
        .visible(checkFov::get)
        .build()
    );
    private final Setting<Boolean> ignoreWalls = sgGeneral.add(new BoolSetting.Builder()
        .name("ignore-walls")
        .description("Whether or not to ignore aiming through walls.")
        .defaultValue(false)
        .build()
    );
    private final Setting<SortPriority> priority = sgGeneral.add(new EnumSetting.Builder<SortPriority>()
        .name("priority")
        .description("How to filter targets within range.")
        .defaultValue(SortPriority.LowestHealth)
        .build()
    );
    private final Setting<Target> bodyTarget = sgGeneral.add(new EnumSetting.Builder<Target>()
        .name("aim-target")
        .description("Which part of the entities body to aim at.")
        .defaultValue(Target.Body)
        .build()
    );

    //--------------------Aim Speed--------------------//
    private final Setting<Boolean> instant = sgSpeed.add(new BoolSetting.Builder()
        .name("instant-look")
        .description("Instantly looks at the entity.")
        .defaultValue(false)
        .build()
    );
    private final Setting<Double> speed = sgSpeed.add(new DoubleSetting.Builder()
        .name("speed")
        .description("How fast to aim at the entity.")
        .defaultValue(5)
        .min(0)
        .visible(() -> !instant.get())
        .build()
    );

    private final Vector3d vec3d1 = new Vector3d();
    private Entity target;

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (!Utils.canUpdate()) return;

        target = TargetUtils.get(entity -> {
            if (!entity.isAlive()) return false;
            if (!PlayerUtils.isWithin(entity, SettingUtils.getAttackRange())) return false;
            if (!ignoreWalls.get() && !PlayerUtils.canSeeEntity(entity)) return false;
            if (entity == mc.player || !entities.get().contains(entity.getType())) return false;
            if (entity instanceof PlayerEntity) return Friends.get().shouldAttack((PlayerEntity) entity);
            return checkFov.get() && PlayerUtils.inFov(entity, fov.get());
        }, priority.get());
    }

    @EventHandler
    private void onRender(Render3DEvent event) {
        if (target != null) aim(target, event.tickDelta, instant.get());
    }

    private void aim(Entity target, double delta, boolean instant) {
        Utils.set(vec3d1, target, delta);

        switch (bodyTarget.get()) {
            case Head -> vec3d1.add(0, target.getEyeHeight(target.getPose()), 0);
            case Body -> vec3d1.add(0, target.getEyeHeight(target.getPose()) / 2, 0);
        }

        double deltaX = vec3d1.x - mc.player.getX();
        double deltaZ = vec3d1.z - mc.player.getZ();
        double deltaY = vec3d1.y - (mc.player.getY() + mc.player.getEyeHeight(mc.player.getPose()));

        // Yaw
        double angle = Math.toDegrees(Math.atan2(deltaZ, deltaX)) - 90;
        double deltaAngle;
        double toRotate;

        if (instant) {
            mc.player.setYaw((float) angle);
        } else {
            deltaAngle = MathHelper.wrapDegrees(angle - mc.player.getYaw());
            toRotate = speed.get() * (deltaAngle >= 0 ? 1 : -1) * delta;
            if ((toRotate >= 0 && toRotate > deltaAngle) || (toRotate < 0 && toRotate < deltaAngle))
                toRotate = deltaAngle;
            mc.player.setYaw(mc.player.getYaw() + (float) toRotate);
        }

        // Pitch
        double idk = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
        angle = -Math.toDegrees(Math.atan2(deltaY, idk));

        if (instant) {
            mc.player.setPitch((float) angle);
        } else {
            deltaAngle = MathHelper.wrapDegrees(angle - mc.player.getPitch());
            toRotate = speed.get() * (deltaAngle >= 0 ? 1 : -1) * delta;
            if ((toRotate >= 0 && toRotate > deltaAngle) || (toRotate < 0 && toRotate < deltaAngle))
                toRotate = deltaAngle;
            mc.player.setPitch(mc.player.getPitch() + (float) toRotate);
        }
    }

    @Override
    public String getInfoString() {
        return EntityUtils.getName(target);
    }
}
