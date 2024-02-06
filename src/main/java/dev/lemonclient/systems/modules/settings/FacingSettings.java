package dev.lemonclient.systems.modules.settings;

import dev.lemonclient.settings.BoolSetting;
import dev.lemonclient.settings.EnumSetting;
import dev.lemonclient.settings.Setting;
import dev.lemonclient.settings.SettingGroup;
import dev.lemonclient.systems.modules.Categories;
import dev.lemonclient.systems.modules.Module;
import dev.lemonclient.utils.SettingUtils;
import dev.lemonclient.utils.player.CombatInfo;
import dev.lemonclient.utils.world.BlockUtils;
import dev.lemonclient.utils.world.PlaceData;
import net.minecraft.block.AirBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.function.Predicate;

public class FacingSettings extends Module {
    public FacingSettings() {
        super(Categories.Settings, "Facing", "Global facing settings for every lemon module.");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    public final Setting<Boolean> strictDir = sgGeneral.add(new BoolSetting.Builder()
        .name("Strict Direction")
        .description("Doesn't place on faces which aren't in your direction.")
        .defaultValue(false)
        .build()
    );
    public final Setting<Boolean> unblocked = sgGeneral.add(new BoolSetting.Builder()
        .name("Unblocked")
        .description("Doesn't place on faces that have block on them.")
        .defaultValue(false)
        .build()
    );
    public final Setting<Boolean> airPlace = sgGeneral.add(new BoolSetting.Builder()
        .name("Air Place")
        .description("Allows placing blocks in air.")
        .defaultValue(false)
        .build()
    );
    public final Setting<MaxHeight> maxHeight = sgGeneral.add(new EnumSetting.Builder<MaxHeight>()
        .name("Max Height")
        .description("Doesn't place on top sides of blocks at max height. Old: 1.12, New: 1.17+")
        .defaultValue(MaxHeight.New)
        .build()
    );


    public PlaceData getPlaceDataOR(BlockPos pos, Predicate<BlockPos> predicate, boolean ignoreContainers) {
        if (pos == null) {
            return new PlaceData(null, null, false);
        }
        Direction best = null;
        if (mc.world != null && mc.player != null) {
            if (airPlace.get()) {
                return new PlaceData(pos, Direction.DOWN, true);
            } else {
                double cDist = -1;
                for (Direction dir : Direction.values()) {
                    // Doesn't place on top of max height
                    if (heightCheck(pos.offset(dir))) {
                        continue;
                    }

                    // Checks if block is an entity (chests, shulkers)
                    if (ignoreContainers && mc.world.getBlockState(pos.offset(dir)).hasBlockEntity()) {
                        continue;
                    }

                    // Test if there is block in the side and if predicate is valid
                    if (!BlockUtils.solid(pos.offset(dir)) && (predicate != null && !predicate.test(pos.offset(dir)))) {
                        continue;
                    }

                    // Strict dir check (checks if face is on opposite side of the block to player)
                    if (strictDir.get() && !CombatInfo.strictDir(pos.offset(dir), dir.getOpposite())) {
                        continue;
                    }

                    // Only accepts if closer than previous accepted direction
                    double dist = SettingUtils.placeRangeTo(pos.offset(dir));
                    if (dist >= 0 && (cDist < 0 || dist < cDist)) {
                        best = dir;
                        cDist = dist;
                    }
                }
            }
        }
        return best == null ? new PlaceData(null, null, false) : new PlaceData(pos.offset(best), best.getOpposite(), true);
    }

    public PlaceData getPlaceDataAND(BlockPos pos, Predicate<Direction> predicate, Predicate<BlockPos> predicatePos, boolean ignoreContainers) {
        if (pos == null) {
            return new PlaceData(null, null, false);
        }
        Direction best = null;
        if (mc.world != null && mc.player != null) {
            if (airPlace.get()) {
                return new PlaceData(pos, Direction.DOWN, true);
            } else {
                double cDist = -1;
                for (Direction dir : Direction.values()) {
                    // Doesn't place on top of max height
                    if (heightCheck(pos.offset(dir))) {
                        continue;
                    }

                    // Checks if block is an entity (chests, shulkers)
                    if (ignoreContainers && mc.world.getBlockState(pos.offset(dir)).hasBlockEntity()) {
                        continue;
                    }

                    // Test if there is block in the side and if predicate is valid
                    if (!BlockUtils.solid(pos.offset(dir)) || (predicate != null && !predicate.test(dir)) || (predicatePos != null && !predicatePos.test(pos.offset(dir)))) {
                        continue;
                    }

                    // Strict dir check (checks if face is on opposite side of the block to player)
                    if (strictDir.get() && !CombatInfo.strictDir(pos.offset(dir), dir.getOpposite())) {
                        continue;
                    }

                    // Only accepts if closer than previous accepted direction
                    double dist = SettingUtils.placeRangeTo(pos.offset(dir));
                    if (dist >= 0 && (cDist < 0 || dist < cDist)) {
                        best = dir;
                        cDist = dist;
                    }
                }
            }
        }
        return best == null ? new PlaceData(null, null, false) : new PlaceData(pos.offset(best), best.getOpposite(), true);
    }

    public PlaceData getPlaceData(BlockPos pos, boolean ignoreContainers) {
        if (pos == null) {
            return new PlaceData(null, null, false);
        }
        Direction best = null;
        if (mc.world != null && mc.player != null) {
            if (airPlace.get()) {
                return new PlaceData(pos, Direction.DOWN, true);
            } else {
                double cDist = -1;
                for (Direction dir : Direction.values()) {
                    // Doesn't place on top of max height
                    if (heightCheck(pos.offset(dir))) {
                        continue;
                    }

                    // Checks if block is an entity (chests, shulkers)
                    if (ignoreContainers && mc.world.getBlockState(pos.offset(dir)).hasBlockEntity()) {
                        continue;
                    }

                    // Test if there is block in the side and if predicate is valid
                    if (!BlockUtils.solid(pos.offset(dir))) {
                        continue;
                    }

                    // Strict dir check (checks if face is on opposite side of the block to player)
                    if (strictDir.get() && !CombatInfo.strictDir(pos.offset(dir), dir.getOpposite())) {
                        continue;
                    }

                    // Only accepts if closer than previous accepted direction
                    double dist = SettingUtils.placeRangeTo(pos.offset(dir));
                    if (dist >= 0 && (cDist < 0 || dist < cDist)) {
                        best = dir;
                        cDist = dist;
                    }
                }
            }
        }
        return best == null ? new PlaceData(null, null, false) : new PlaceData(pos.offset(best), best.getOpposite(), true);
    }

    public Direction getPlaceOnDirection(BlockPos pos) {
        if (pos == null) {
            return null;
        }
        Direction best = null;
        if (mc.world != null && mc.player != null) {
            double cDist = -1;
            for (Direction dir : Direction.values()) {
                // Doesn't place on top of max height
                if (heightCheck(pos.offset(dir))) {
                    continue;
                }

                // Unblocked check (mostly for autocrystal placement facings)
                if (unblocked.get() && !(mc.world.getBlockState(pos.offset(dir)).getBlock() instanceof AirBlock)) {
                    continue;
                }

                // Strict dir check (checks if face isnt on opposite side of the block to player)
                if (strictDir.get() && !CombatInfo.strictDir(pos, dir)) {
                    continue;
                }

                // Only accepts if closer than last accepted direction
                double dist = dist(pos, dir);
                if (dist >= 0 && (cDist < 0 || dist < cDist)) {
                    best = dir;
                    cDist = dist;
                }
            }
        }
        return best;
    }

    public boolean heightCheck(BlockPos pos) {
        return pos.getY() >= switch (maxHeight.get()) {
            case Old -> 255;
            case New -> 319;
            case Disabled -> 1000; // im pretty sure 1000 is enough
        };
    }

    private double dist(BlockPos pos, Direction dir) {
        if (mc.player == null) {
            return 0;
        }

        Vec3d vec = new Vec3d(pos.getX() + dir.getOffsetX() / 2f, pos.getY() + dir.getOffsetY() / 2f, pos.getZ() + dir.getOffsetZ() / 2f);
        Vec3d dist = mc.player.getEyePos().add(-vec.x, -vec.y, -vec.z);

        return Math.sqrt(dist.x * dist.x + dist.y * dist.y + dist.z * dist.z);
    }

    public enum MaxHeight {
        Old,
        New,
        Disabled
    }
}
