package dev.lemonclient.utils.entity;

import dev.lemonclient.systems.friends.Friends;
import dev.lemonclient.utils.Utils;
import dev.lemonclient.utils.entity.fakeplayer.FakePlayerEntity;
import dev.lemonclient.utils.player.PlayerUtils;
import dev.lemonclient.utils.player.Rotations;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.GameMode;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static dev.lemonclient.LemonClient.mc;

public class TargetUtils {
    private static final List<Entity> ENTITIES = new ArrayList<>();

    public static Entity get(Predicate<Entity> isGood, SortPriority sortPriority) {
        ENTITIES.clear();
        getList(ENTITIES, isGood, sortPriority, 1);
        if (!ENTITIES.isEmpty()) {
            return ENTITIES.get(0);
        }

        return null;
    }

    public static void getList(List<Entity> targetList, Predicate<Entity> isGood, SortPriority sortPriority, int maxCount) {
        targetList.clear();

        for (Entity entity : mc.world.getEntities()) {
            if (entity != null && isGood.test(entity)) targetList.add(entity);
        }

        targetList.sort((e1, e2) -> sort(e1, e2, sortPriority));
        targetList.removeIf(entity -> targetList.indexOf(entity) > maxCount - 1);
    }

    public static PlayerEntity getPlayerTarget(double range, SortPriority priority) {
        if (!Utils.canUpdate()) return null;
        return (PlayerEntity) get(entity -> {
            if (!(entity instanceof PlayerEntity) || entity == mc.player) return false;
            if (((PlayerEntity) entity).isDead() || ((PlayerEntity) entity).getHealth() <= 0) return false;
            if (!PlayerUtils.isWithin(entity, range)) return false;
            if (!Friends.get().shouldAttack((PlayerEntity) entity)) return false;
            return EntityUtils.getGameMode((PlayerEntity) entity) == GameMode.SURVIVAL || entity instanceof FakePlayerEntity;
        }, priority);
    }

    public static PlayerEntity getPlayerTarget() {
        if (!Utils.canUpdate()) return null;
        return (PlayerEntity) get(entity -> {
            if (!(entity instanceof PlayerEntity) || entity == mc.player) return false;
            if (((PlayerEntity) entity).isDead() || ((PlayerEntity) entity).getHealth() <= 0) return false;
            if (!Friends.get().shouldAttack((PlayerEntity) entity)) return false;
            return EntityUtils.getGameMode((PlayerEntity) entity) == GameMode.SURVIVAL || entity instanceof FakePlayerEntity;
        }, SortPriority.LowestDistance);
    }

    public static boolean isBadTarget(PlayerEntity target, double range) {
        if (target == null) return true;
        return !PlayerUtils.isWithin(target, range) || !target.isAlive() || target.isDead() || target.getHealth() <= 0;
    }

    private static int sort(Entity e1, Entity e2, SortPriority priority) {
        return switch (priority) {
            case LowestDistance -> Double.compare(PlayerUtils.squaredDistanceTo(e1), PlayerUtils.squaredDistanceTo(e2));
            case HighestDistance ->
                Double.compare(PlayerUtils.squaredDistanceTo(e2), PlayerUtils.squaredDistanceTo(e1));
            case LowestHealth -> sortHealth(e1, e2);
            case HighestHealth -> sortHealth(e2, e1);
            case ClosestAngle -> sortAngle(e1, e2);
        };
    }

    private static int sortHealth(Entity e1, Entity e2) {
        boolean e1l = e1 instanceof LivingEntity;
        boolean e2l = e2 instanceof LivingEntity;

        if (!e1l && !e2l) return 0;
        else if (e1l && !e2l) return 1;
        else if (!e1l) return -1;

        return Float.compare(((LivingEntity) e1).getHealth(), ((LivingEntity) e2).getHealth());
    }

    private static int sortAngle(Entity e1, Entity e2) {
        boolean e1l = e1 instanceof LivingEntity;
        boolean e2l = e2 instanceof LivingEntity;

        if (!e1l && !e2l) return 0;
        else if (e1l && !e2l) return 1;
        else if (!e1l) return -1;

        double e1yaw = Math.abs(Rotations.getYaw(e1) - mc.player.getYaw());
        double e2yaw = Math.abs(Rotations.getYaw(e2) - mc.player.getYaw());

        double e1pitch = Math.abs(Rotations.getPitch(e1) - mc.player.getPitch());
        double e2pitch = Math.abs(Rotations.getPitch(e2) - mc.player.getPitch());

        return Double.compare(e1yaw * e1yaw + e1pitch * e1pitch, e2yaw * e2yaw + e2pitch * e2pitch);
    }
}
