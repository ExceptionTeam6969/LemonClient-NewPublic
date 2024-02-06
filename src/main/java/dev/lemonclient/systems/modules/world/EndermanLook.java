package dev.lemonclient.systems.modules.world;

import dev.lemonclient.events.world.TickEvent;
import dev.lemonclient.settings.EnumSetting;
import dev.lemonclient.settings.Setting;
import dev.lemonclient.settings.SettingGroup;
import dev.lemonclient.systems.modules.Categories;
import dev.lemonclient.systems.modules.Module;
import dev.lemonclient.utils.entity.Target;
import dev.lemonclient.utils.player.Rotations;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.EndermanEntity;
import net.minecraft.util.math.Vec3d;

public class EndermanLook extends Module {
    public EndermanLook() {
        super(Categories.World, "Enderman Look", "Either looks at all Endermen or prevents you from looking at Endermen.");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Mode> lookMode = sgGeneral.add(new EnumSetting.Builder<Mode>()
        .name("look-mode")
        .description("How this module behaves.")
        .defaultValue(Mode.Away)
        .build()
    );

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (lookMode.get() == Mode.Away) {
            if (mc.player.getAbilities().creativeMode || !shouldLook()) return;

            Rotations.rotate(mc.player.getYaw(), 90, -75, null);
        } else {
            for (Entity entity : mc.world.getEntities()) {
                if (!(entity instanceof EndermanEntity enderman)) continue;

                if (enderman.isAngry() || !enderman.isAlive() || !mc.player.canSee(enderman)) continue;

                Rotations.rotate(Rotations.getYaw(enderman), Rotations.getPitch(enderman, Target.Head), -75, null);
                break;
            }
        }
    }

    private boolean shouldLook() {
        for (Entity entity : mc.world.getEntities()) {
            if (!(entity instanceof EndermanEntity)) continue;

            if (entity.isAlive() && angleCheck(entity)) return true;
        }

        return false;
    }

    private boolean angleCheck(Entity entity) {
        Vec3d vec3d = mc.player.getRotationVec(1.0F).normalize();
        Vec3d vec3d2 = new Vec3d(entity.getX() - mc.player.getX(), entity.getEyeY() - mc.player.getEyeY(), entity.getZ() - mc.player.getZ());

        double d = vec3d2.length();
        vec3d2 = vec3d2.normalize();
        double e = vec3d.dotProduct(vec3d2);

        return e > 1.0D - 0.025D / d && mc.player.canSee(entity);
    }

    public enum Mode {
        At,
        Away
    }
}
