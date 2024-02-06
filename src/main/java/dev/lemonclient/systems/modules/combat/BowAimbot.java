package dev.lemonclient.systems.modules.combat;

import dev.lemonclient.enums.RotationType;
import dev.lemonclient.events.render.Render3DEvent;
import dev.lemonclient.managers.Managers;
import dev.lemonclient.pathing.PathManagers;
import dev.lemonclient.settings.*;
import dev.lemonclient.systems.friends.Friends;
import dev.lemonclient.systems.modules.Categories;
import dev.lemonclient.systems.modules.Module;
import dev.lemonclient.utils.entity.EntityUtils;
import dev.lemonclient.utils.entity.SortPriority;
import dev.lemonclient.utils.entity.TargetUtils;
import dev.lemonclient.utils.player.InvUtils;
import dev.lemonclient.utils.player.PlayerUtils;
import dev.lemonclient.utils.player.Rotations;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArrowItem;
import net.minecraft.item.Items;
import net.minecraft.util.math.Vec3d;

import java.util.Objects;
import java.util.Set;

public class BowAimbot extends Module {
    public BowAimbot() {
        super(Categories.Combat, "Bow Aimbot", "Automatically aims your bow for you.");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Double> range = sgGeneral.add(new DoubleSetting.Builder()
        .name("range")
        .description("The maximum range the entity can be to aim at it.")
        .defaultValue(20)
        .range(0, 100)
        .sliderMax(100)
        .build()
    );

    private final Setting<Set<EntityType<?>>> entities = sgGeneral.add(new EntityTypeListSetting.Builder()
        .name("entities")
        .description("Entities to attack.")
        .onlyAttackable()
        .build()
    );

    private final Setting<SortPriority> sortPriority = sgGeneral.add(new EnumSetting.Builder<SortPriority>()
        .name("priority")
        .description("What type of entities to target.")
        .defaultValue(SortPriority.LowestHealth)
        .build()
    );

    private final Setting<Boolean> babies = sgGeneral.add(new BoolSetting.Builder()
        .name("babies")
        .description("Whether or not to attack baby variants of the entity.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> nametagged = sgGeneral.add(new BoolSetting.Builder()
        .name("nametagged")
        .description("Whether or not to attack mobs with a name tag.")
        .defaultValue(false)
        .build()
    );


    private final Setting<Boolean> pauseOnCombat = sgGeneral.add(new BoolSetting.Builder()
        .name("pause-on-combat")
        .description("Freezes Baritone temporarily until you released the bow.")
        .defaultValue(false)
        .build()
    );

    private boolean wasPathing;
    private Entity target;

    @Override
    public void onDeactivate() {
        target = null;
        wasPathing = false;
    }

    @EventHandler
    private void onRender(Render3DEvent event) {
        if (!PlayerUtils.isAlive() || !itemInHand()) return;
        if (!InvUtils.find(itemStack -> itemStack.getItem() instanceof ArrowItem).found()) return;

        target = TargetUtils.get(entity -> {
            if (entity == mc.player || entity == mc.cameraEntity) return false;
            if ((entity instanceof LivingEntity && ((LivingEntity) entity).isDead()) || !entity.isAlive()) return false;
            if (!PlayerUtils.isWithin(entity, range.get())) return false;
            if (!entities.get().contains(entity.getType())) return false;
            if (!nametagged.get() && entity.hasCustomName()) return false;
            if (!PlayerUtils.canSeeEntity(entity)) return false;
            if (entity instanceof PlayerEntity) {
                if (((PlayerEntity) entity).isCreative()) return false;
                if (!Friends.get().shouldAttack((PlayerEntity) entity)) return false;
            }
            return !(entity instanceof AnimalEntity) || babies.get() || !((AnimalEntity) entity).isBaby();
        }, sortPriority.get());

        if (target == null) {
            if (wasPathing) {
                PathManagers.get().resume();
                wasPathing = false;
            }
            return;
        }

        if (mc.options.useKey.isPressed() && itemInHand()) {
            if (pauseOnCombat.get() && PathManagers.get().isPathing() && !wasPathing) {
                PathManagers.get().pause();
                wasPathing = true;
            }
            aim(event.tickDelta);
        }
    }

    private boolean itemInHand() {
        return InvUtils.testInMainHand(Items.BOW, Items.CROSSBOW);
    }

    private void aim(double tickDelta) {
        // Velocity based on bow charge.
        float velocity = (mc.player.getItemUseTime() - mc.player.getItemUseTimeLeft()) / 20f;
        velocity = (velocity * velocity + velocity * 2) / 3;
        if (velocity > 1) velocity = 1;

        // Positions
        double posX = target.getPos().getX() + (target.getPos().getX() - target.prevX) * tickDelta;
        double posY = target.getPos().getY() + (target.getPos().getY() - target.prevY) * tickDelta;
        double posZ = target.getPos().getZ() + (target.getPos().getZ() - target.prevZ) * tickDelta;

        // Adjusting for hitbox heights
        posY -= 1.9f - target.getHeight();

        double relativeX = posX - mc.player.getX();
        double relativeY = posY - mc.player.getY();
        double relativeZ = posZ - mc.player.getZ();

        // Calculate the pitch
        double hDistance = Math.sqrt(relativeX * relativeX + relativeZ * relativeZ);
        double hDistanceSq = hDistance * hDistance;
        float g = 0.006f;
        float velocitySq = velocity * velocity;
        float pitch = (float) -Math.toDegrees(Math.atan((velocitySq - Math.sqrt(velocitySq * velocitySq - g * (g * hDistanceSq + 2 * relativeY * velocitySq))) / (g * hDistance)));

        // Set player rotation
        if (Float.isNaN(pitch)) {
            Managers.ROTATION.start(target.getBoundingBox(), priority, RotationType.Other, Objects.hash(name + "aim"));
        } else {
            Managers.ROTATION.start(Rotations.getYaw(new Vec3d(posX, posY, posZ)), pitch, priority, RotationType.Other, Objects.hash(name + "aim"));
        }
    }

    @Override
    public String getInfoString() {
        return EntityUtils.getName(target);
    }
}
