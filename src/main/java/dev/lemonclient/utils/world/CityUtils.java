package dev.lemonclient.utils.world;

import dev.lemonclient.utils.player.PlayerUtils;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static dev.lemonclient.LemonClient.mc;
import static dev.lemonclient.utils.misc.Vec3dInfo.getEyeVec;
import static dev.lemonclient.utils.world.BlockInfo.isCombatBlock;

public class CityUtils {
    public static List<BlockPos> getCityList(PlayerEntity target) {
        if (target == null) return null;

        List<BlockPos> blockArray = new ArrayList<>();
        BlockPos tPos = target.getBlockPos();

        for (Direction dir : Direction.values()) {
            if (dir.equals(Direction.UP) || dir.equals(Direction.DOWN)) continue;

            if (isCombatBlock(tPos.offset(dir))) blockArray.add(tPos.offset(dir));
        }

        if (blockArray.isEmpty()) return null;

        BlockPos prevBP = blockArray.get(0);
        blockArray.removeIf(CityUtils::isOurSurround);

        if (blockArray.isEmpty()) blockArray.add(prevBP);
        blockArray.sort(Comparator.comparingDouble(PlayerUtils::distanceTo));
        return blockArray;
    }

    public static BlockPos getCrystalPos(BlockPos breakPos, boolean support) {
        if (breakPos != null) return null;
        ArrayList<BlockPos> blockPos = new ArrayList<>();

        for (Direction direction : Direction.values()) {
            if (direction.equals(Direction.UP) || direction.equals(Direction.DOWN)) continue;

            if (canCrystal(breakPos.offset(direction), support)) blockPos.add(breakPos.offset(direction));
        }

        blockPos.sort(Comparator.comparingDouble(PlayerUtils::distanceTo));
        return blockPos.isEmpty() ? null : blockPos.get(0).down();
    }

    private static boolean canCrystal(BlockPos blockPos, boolean support) {
        for (Entity entity : mc.world.getEntities()) {
            if (entity instanceof PlayerEntity || entity instanceof EndCrystalEntity || entity instanceof ItemEntity) {
                if (entity.getBlockPos().equals(blockPos)) return false;
            }
        }

        return mc.world.getBlockState(blockPos).isOf(Blocks.AIR) && (support ? (mc.world.getBlockState(blockPos.down()).isOf(Blocks.AIR) || (mc.world.getBlockState(blockPos.down())).isOf(Blocks.OBSIDIAN) || mc.world.getBlockState(blockPos.down()).isOf(Blocks.BEDROCK)) : (mc.world.getBlockState(blockPos.down()).isOf(Blocks.OBSIDIAN) || mc.world.getBlockState(blockPos.down()).isOf(Blocks.BEDROCK)));
    }

    public static boolean isOurSurround(BlockPos blockPos) {
        BlockPos pos = mc.player.getBlockPos();
        for (Direction dir : Direction.values()) {
            if (dir == Direction.UP || dir == Direction.DOWN) continue;
            if (pos.offset(dir).equals(blockPos)) return true;
        }
        return false;
    }

    public static Direction getDirection(BlockPos pos) {
        if (pos == null) return Direction.UP;
        Vec3d eyesPos = getEyeVec(mc.player);
        for (Direction direction : Direction.values()) {
            RaycastContext raycastContext = new RaycastContext(eyesPos, new Vec3d(pos.getX() + 0.5 + direction.getVector().getX() * 0.5,
                pos.getY() + 0.5 + direction.getVector().getY() * 0.5,
                pos.getZ() + 0.5 + direction.getVector().getZ() * 0.5), RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, mc.player);
            BlockHitResult result = mc.world.raycast(raycastContext);
            if (result != null && result.getType() == HitResult.Type.BLOCK && result.getBlockPos().equals(pos)) {
                return direction;
            }
        }
        if ((double) pos.getY() > eyesPos.y) {
            return Direction.DOWN;
        }
        return Direction.UP;
    }
}
