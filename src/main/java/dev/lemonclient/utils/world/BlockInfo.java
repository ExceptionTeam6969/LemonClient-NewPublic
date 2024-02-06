package dev.lemonclient.utils.world;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.effect.StatusEffectUtil;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;

import java.util.ArrayList;
import java.util.List;

import static dev.lemonclient.LemonClient.mc;

public class BlockInfo {
    public static double progress = 0;

    public static VoxelShape getShape(BlockPos block) {
        return mc.world.getBlockState(block).getOutlineShape(mc.world, block);
    }

    public static Box getBox(BlockPos block) {
        return getShape(block).getBoundingBox();
    }

    public static BlockState getBlockState(BlockPos block) {
        return mc.world.getBlockState(block);
    }

    public static Block getBlock(BlockPos block) {
        return mc.world.getBlockState(block).getBlock();
    }

    public static float getBlastResistance(BlockPos block) {
        return mc.world.getBlockState(block).getBlock().getBlastResistance();
    }

    public static boolean canBreak(int slot, BlockPos blockPos) {
        if (progress >= 1) return true;
        BlockState blockState = mc.world.getBlockState(blockPos);

        if (progress < 1)
            progress += BlockUtils.getBreakDelta(slot != 420 ? slot : mc.player.getInventory().selectedSlot, blockState);
        return false;
    }

    public static boolean isSolid(BlockPos block) {
        return mc.world.getBlockState(block).isSolid();
    }

    public static boolean isBurnable(BlockPos block) {
        return mc.world.getBlockState(block).isBurnable();
    }

    public static boolean isLiquid(BlockPos block) {
        return mc.world.getBlockState(block).isLiquid();
    }

    public static float getHardness(BlockPos block) {
        return mc.world.getBlockState(block).getHardness(mc.world, block);
    }

    public static float getHardness(Block block) {
        return block.getHardness();
    }

    public static boolean isBlastResist(BlockPos block) {
        return getBlastResistance(block) >= 600;
    }

    public static boolean isBlastResist(Block block) {
        return block.getBlastResistance() >= 600;
    }

    public static boolean isBreakable(BlockPos pos) {
        return getHardness(pos) > 0;
    }

    public static boolean isBreakable(Block block) {
        return getHardness(block) > 0;
    }

    public static boolean isCombatBlock(BlockPos block) {
        return isBlastResist(block) && isBreakable(block);
    }

    public static boolean isCombatBlock(Block block) {
        return isBlastResist(block) && isBreakable(block);
    }

    public static boolean isFullCube(BlockPos block) {
        return mc.world.getBlockState(block).isFullCube(mc.world, block);
    }

    public static int getBlockBreakingSpeed(BlockState block, BlockPos pos, int slot) {
        PlayerEntity player = mc.player;

        float f = (player.getInventory().getStack(slot)).getMiningSpeedMultiplier(block);
        if (f > 1.0F) {
            int i = EnchantmentHelper.get(player.getInventory().getStack(slot)).getOrDefault(Enchantments.EFFICIENCY, 0);
            if (i > 0) {
                f += (float) (i * i + 1);
            }
        }

        if (StatusEffectUtil.hasHaste(player)) {
            f *= 1.0F + (float) (StatusEffectUtil.getHasteAmplifier(player) + 1) * 0.2F;
        }

        if (player.hasStatusEffect(StatusEffects.MINING_FATIGUE)) {
            float k = switch (player.getStatusEffect(StatusEffects.MINING_FATIGUE).getAmplifier()) {
                case 0 -> 0.3F;
                case 1 -> 0.09F;
                case 2 -> 0.0027F;
                default -> 8.1E-4F;
            };

            f *= k;
        }

        if (player.isSubmergedIn(FluidTags.WATER) && !EnchantmentHelper.hasAquaAffinity(player)) {
            f /= 5.0F;
        }

        if (!player.isOnGround()) {
            f /= 5.0F;
        }

        float t = block.getHardness(mc.world, pos);
        if (t == -1.0F) {
            return 0;
        } else {
            return (int) Math.ceil(1 / (f / t / 30));
        }
    }

    public static Block getBoxTouchedBlock(Box box) {
        Block block = null;

        for (int x = (int) Math.floor(box.minX); x < Math.ceil(box.maxX); x++) {
            for (int y = (int) Math.floor(box.minY); y < Math.ceil(box.maxY); y++) {
                for (int z = (int) Math.floor(box.minZ); z < Math.ceil(box.maxZ); z++) {
                    block = mc.world.getBlockState(new BlockPos(x, y, z)).getBlock();
                }
            }
        }

        return block;
    }

    public static boolean doesBoxTouchBlock(Box box, Block block) {
        for (int x = (int) Math.floor(box.minX); x < Math.ceil(box.maxX); x++) {
            for (int y = (int) Math.floor(box.minY); y < Math.ceil(box.maxY); y++) {
                for (int z = (int) Math.floor(box.minZ); z < Math.ceil(box.maxZ); z++) {
                    if (mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() == block) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public static Vec3d closestVec3d(BlockPos blockPos) {
        if (blockPos == null) return new Vec3d(0.0, 0.0, 0.0);
        double x = MathHelper.clamp((mc.player.getX() - blockPos.getX()), 0.0, 1.0);
        double y = MathHelper.clamp((mc.player.getY() - blockPos.getY()), 0.0, 0.6);
        double z = MathHelper.clamp((mc.player.getZ() - blockPos.getZ()), 0.0, 1.0);
        return new Vec3d(blockPos.getX() + x, blockPos.getY() + y, blockPos.getZ() + z);
    }

    private static Vec3d closestVec3d(Box box) {
        if (box == null) return new Vec3d(0.0, 0.0, 0.0);
        Vec3d eyePos = new Vec3d(mc.player.getX(), mc.player.getY() + mc.player.getEyeHeight(mc.player.getPose()), mc.player.getZ());

        double x = MathHelper.clamp(eyePos.getX(), box.minX, box.maxX);
        double y = MathHelper.clamp(eyePos.getY(), box.minY, box.maxY);
        double z = MathHelper.clamp(eyePos.getZ(), box.minZ, box.maxZ);

        return new Vec3d(x, y, z);
    }

    public static Vec3d closestVec3d2(BlockPos pos) {
        return closestVec3d(box(pos));
    }

    public static List<BlockPos> getSphere(BlockPos centerPos, int radius, int height) {
        final List<BlockPos> blocks = new ArrayList<>();

        for (int i = centerPos.getX() - radius; i < centerPos.getX() + radius; i++) {
            for (int j = centerPos.getY() - height; j < centerPos.getY() + height; j++) {
                for (int k = centerPos.getZ() - radius; k < centerPos.getZ() + radius; k++) {
                    BlockPos pos = new BlockPos(i, j, k);
                    if (distanceBetween(centerPos, pos) <= radius && !blocks.contains(pos)) {
                        blocks.add(pos);
                    }
                }
            }
        }

        return blocks;
    }

    public static List<BlockPos> getSphere(BlockPos centerPos, double radius, double height) {
        ArrayList<BlockPos> blocks = new ArrayList<>();

        for (int i = centerPos.getX() - (int) radius; i < centerPos.getX() + radius; i++) {
            for (int j = centerPos.getY() - (int) height; j < centerPos.getY() + height; j++) {
                for (int k = centerPos.getZ() - (int) radius; k < centerPos.getZ() + radius; k++) {
                    BlockPos pos = new BlockPos(i, j, k);

                    if (distanceBetween(centerPos, pos) <= radius && !blocks.contains(pos)) blocks.add(pos);
                }
            }
        }

        return blocks;
    }

    public static double distanceBetween(BlockPos blockPos1, BlockPos blockPos2) {
        double d = blockPos1.getX() - blockPos2.getX();
        double e = blockPos1.getY() - blockPos2.getY();
        double f = blockPos1.getZ() - blockPos2.getZ();
        return MathHelper.sqrt((float) (d * d + e * e + f * f));
    }

    public static Box box(BlockPos blockPos) {
        return new Box(blockPos.getX(), blockPos.getY(), blockPos.getZ(), blockPos.getX() + 1, blockPos.getY() + 1, blockPos.getZ() + 1);
    }

    public static BlockPos roundBlockPos(Vec3d vec) {
        return BlockPos.ofFloored(vec.x, Math.round(vec.y), vec.z);
    }
}
