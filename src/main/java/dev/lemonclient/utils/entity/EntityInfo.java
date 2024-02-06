package dev.lemonclient.utils.entity;

import dev.lemonclient.utils.Utils;
import dev.lemonclient.utils.world.BlockInfo;
import net.minecraft.block.AirBlock;
import net.minecraft.block.AnvilBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.*;
import net.minecraft.world.RaycastContext;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import static dev.lemonclient.LemonClient.mc;
import static dev.lemonclient.utils.world.BlockInfo.isBlastResist;

public class EntityInfo {
    public static boolean isWebbed(PlayerEntity entity) {
        return BlockInfo.doesBoxTouchBlock(entity.getBoundingBox(), Blocks.COBWEB);
    }

    public static boolean isBurrowed(PlayerEntity entity) {
        BlockPos selfPos = getFillBlock(entity);
        return mc.world.getBlockState(selfPos).getBlock() == Blocks.OBSIDIAN || mc.world.getBlockState(selfPos.up()).getBlock() == Blocks.ENDER_CHEST || mc.world.getBlockState(selfPos).getBlock() == Blocks.BEDROCK;
    }

    public static boolean isBurrowed(PlayerEntity entity, BlastResistantType type) {
        return isBlastResistant(getFillBlock(entity), type);
    }

    public static BlockPos getFillBlock(PlayerEntity player) {
        return getFillBlock(player, 0);
    }

    public static BlockPos getFillBlock(PlayerEntity player, int yOff) {
        LinkedHashSet<BlockPos> feetBlock = new LinkedHashSet<>();
        feetBlock.add(BlockPos.ofFloored(player.getPos().add(0.0d, yOff, 0.0d)));
        feetBlock.add(BlockPos.ofFloored(player.getPos().add(0.3d, yOff, 0.3d)));
        feetBlock.add(BlockPos.ofFloored(player.getPos().add(-0.3d, yOff, 0.3d)));
        feetBlock.add(BlockPos.ofFloored(player.getPos().add(0.3d, yOff, -0.3d)));
        feetBlock.add(BlockPos.ofFloored(player.getPos().add(-0.3d, yOff, -0.3d)));

        List<BlockPos> collect = feetBlock.stream().limit(1L).toList();
        if (collect.size() == 0) {
            return null;
        }
        return collect.get(0);
    }

    public static boolean isBlastResistant(BlockPos pos, BlastResistantType type) {
        Block block = mc.world.getBlockState(pos).getBlock();
        switch (type) {
            case Any, Mineable -> {
                return block == Blocks.OBSIDIAN
                    || block == Blocks.CRYING_OBSIDIAN
                    || block instanceof AnvilBlock
                    || block == Blocks.NETHERITE_BLOCK
                    || block == Blocks.ENDER_CHEST
                    || block == Blocks.RESPAWN_ANCHOR
                    || block == Blocks.ANCIENT_DEBRIS
                    || block == Blocks.ENCHANTING_TABLE
                    || (block == Blocks.BEDROCK && type == BlastResistantType.Any)
                    || (block == Blocks.END_PORTAL_FRAME && type == BlastResistantType.Any);
            }
            case Unbreakable -> {
                return block == Blocks.BEDROCK
                    || block == Blocks.END_PORTAL_FRAME;
            }
            case NotAir -> {
                return block != Blocks.AIR;
            }
        }
        return false;
    }

    public static boolean isInHole(PlayerEntity p) {
        BlockPos pos = p.getBlockPos();
        return !mc.world.getBlockState(pos.add(1, 0, 0)).isAir()
            && !mc.world.getBlockState(pos.add(-1, 0, 0)).isAir()
            && !mc.world.getBlockState(pos.add(0, 0, 1)).isAir()
            && !mc.world.getBlockState(pos.add(0, 0, -1)).isAir()
            && !mc.world.getBlockState(pos.add(0, -1, 0)).isAir();
    }

    public static boolean isInLiquid() {
        if (mc.player.fallDistance >= 3.0f) {
            return false;
        }
        boolean inLiquid = false;
        Box bb = /*mc.player.getRidingEntity() != null ? mc.player.getRidingEntity().getEntityBoundingBox() : */mc.player.getBoundingBox();
        int y = (int) bb.minY;
        for (int x = MathHelper.floor(bb.minX); x < MathHelper.floor(bb.maxX) + 1; ++x) {
            for (int z = MathHelper.floor(bb.minZ); z < MathHelper.floor(bb.maxZ) + 1; ++z) {
                Block block = mc.world.getBlockState(new BlockPos(x, y, z)).getBlock();
                if (block instanceof AirBlock) continue;
                if (!(block == Blocks.WATER)) {
                    return false;
                }
                inLiquid = true;
            }
        }
        return inLiquid;
    }

    public static BlockPos playerPos(PlayerEntity targetEntity) {
        return BlockInfo.roundBlockPos(targetEntity.getPos());
    }

    public static boolean isInHole(PlayerEntity targetEntity, boolean doubles, BlastResistantType type) {
        if (!Utils.canUpdate()) return false;

        BlockPos blockPos = playerPos(targetEntity);
        int air = 0;

        for (Direction direction : Direction.values()) {
            if (direction == Direction.UP) continue;

            if (!isBlastResistant(blockPos.offset(direction), type)) {
                if (!doubles || direction == Direction.DOWN) return false;

                air++;

                for (Direction dir : Direction.values()) {
                    if (dir == direction.getOpposite() || dir == Direction.UP) continue;

                    if (!isBlastResistant(blockPos.offset(direction).offset(dir), type)) {
                        return false;
                    }
                }
            }
        }

        return air < 2;
    }

    public static List<BlockPos> getBlocksAround(PlayerEntity player) {
        List<BlockPos> positions = new ArrayList<>();
        List<Entity> getEntityBoxes;

        for (BlockPos blockPos : BlockInfo.getSphere(player.getBlockPos(), 3, 1)) {
            getEntityBoxes = mc.world.getOtherEntities(null, new Box(blockPos), entity -> entity == player);
            if (!getEntityBoxes.isEmpty()) continue;

            for (Direction direction : Direction.values()) {
                if (direction == Direction.UP || direction == Direction.DOWN) continue;

                getEntityBoxes = mc.world.getOtherEntities(null, new Box(blockPos.offset(direction)), entity -> entity == player);
                if (!getEntityBoxes.isEmpty()) positions.add(new BlockPos(blockPos));
            }
        }

        return positions;
    }

    public static Direction rayTraceCheck(BlockPos pos, boolean forceReturn) {
        Vec3d eyesPos = new Vec3d(mc.player.getX(), mc.player.getY() + (double) mc.player.getEyeHeight(mc.player.getPose()), mc.player.getZ());

        for (Direction direction : Direction.values()) {
            RaycastContext raycastContext = new RaycastContext(eyesPos, new Vec3d((double) pos.getX() + 0.5D + (double) direction.getVector().getX() * 0.5D, (double) pos.getY() + 0.5D + (double) direction.getVector().getY() * 0.5D, (double) pos.getZ() + 0.5D + (double) direction.getVector().getZ() * 0.5D), RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, mc.player);
            BlockHitResult result = mc.world.raycast(raycastContext);
            if (result != null && result.getType() == HitResult.Type.BLOCK && result.getBlockPos().equals(pos)) {
                return direction;
            }
        }

        if (forceReturn) {
            if ((double) pos.getY() > eyesPos.y) {
                return Direction.DOWN;
            } else {
                return Direction.UP;
            }
        } else {
            return null;
        }
    }

    public static boolean isDoubleSurrounded(LivingEntity entity) {
        BlockPos blockPos = entity.getBlockPos();
        return isBlastResist(blockPos.add(1, 0, 0)) &&
            isBlastResist(blockPos.add(-1, 0, 0)) &&
            isBlastResist(blockPos.add(0, 0, 1)) &&
            isBlastResist(blockPos.add(0, 0, -1)) &&
            isBlastResist(blockPos.add(1, 1, 0)) &&
            isBlastResist(blockPos.add(-1, 1, 0)) &&
            isBlastResist(blockPos.add(0, 1, 1)) &&
            isBlastResist(blockPos.add(0, 1, -1));
    }

    public enum BlastResistantType {
        Any, // Any blast resistant block
        Unbreakable, // Can't be mined
        Mineable, // You can mine the block
        NotAir // Doesn't matter as long it's not air
    }
}
