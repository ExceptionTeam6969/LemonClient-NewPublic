package dev.lemonclient.utils.player;

import dev.lemonclient.utils.world.BlockInfo;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.*;
import net.minecraft.world.RaycastContext;

import static dev.lemonclient.LemonClient.mc;

public class RaycastUtils {
    public static HitResult raycastVanilla(float yaw, float pitch, double maxDistance, boolean includeFluids) {
        Vec3d eyePos = mc.player.getCameraPosVec(1f);
        Vec3d lookVec = getRotationVector(yaw, pitch);
        Vec3d endReachPos = eyePos.add(lookVec.x * maxDistance, lookVec.y * maxDistance, lookVec.z * maxDistance);
        return mc.world.raycast(new RaycastContext(eyePos, endReachPos, RaycastContext.ShapeType.OUTLINE, includeFluids ? RaycastContext.FluidHandling.ANY : RaycastContext.FluidHandling.NONE, mc.player));
    }

    public static HitResult raycast(float yaw, float pitch, double reachDistance, boolean isRayBlock) {
        float tickDelta = 1f;
        Entity entity = mc.getCameraEntity();
        HitResult result = null;
        if (entity != null) {
            if (mc.world != null) {
                result = raycastVanilla(yaw, pitch, reachDistance, false);
                Vec3d eyePos = entity.getCameraPosVec(tickDelta);
                Vec3d rotationVec = getRotationVector(yaw, pitch);
                Vec3d endReachPos = eyePos.add(rotationVec.x * reachDistance, rotationVec.y * reachDistance, rotationVec.z * reachDistance);
                Box box = entity.getBoundingBox().stretch(rotationVec.multiply(reachDistance)).expand(1.0, 1.0, 1.0);
                EntityHitResult entityHitResult = ProjectileUtil.raycast(entity, eyePos, endReachPos, box, (e) -> !e.isSpectator() && e.canHit(), reachDistance);
                if (entityHitResult != null) {
                    Vec3d reachedPosition = entityHitResult.getPos();
                    double distance = eyePos.squaredDistanceTo(reachedPosition);
                    if (isRayBlock) {
                        Direction lookDir = getLookDirection(rotationVec);
                        if (distance > reachDistance) {
                            result = BlockHitResult.createMissed(reachedPosition, lookDir, BlockPos.ofFloored(reachedPosition));
                        } else if (distance <= reachDistance) {
                            result = new BlockHitResult(reachedPosition, lookDir, BlockPos.ofFloored(reachedPosition), mc.player.isInsideWall());
                        }
                    } else if (distance < reachDistance || result == null) {
                        result = entityHitResult;
                    }
                }
            }
        }
        return result;
    }

    public static Entity getRayEntity(float yaw, float pitch, double reachDistance) {
        HitResult result = raycast(yaw, pitch, reachDistance, false);
        if (result == null || !result.getType().equals(HitResult.Type.ENTITY)) return null;
        return ((EntityHitResult) result).getEntity();
    }

    public static Block getRayBlock(float yaw, float pitch, double reachDistance) {
        BlockPos pos = getRayBlockPos(yaw, pitch, reachDistance);
        return pos == null ? Blocks.AIR : BlockInfo.getBlock(pos);
    }

    public static Direction getRayDirection(float yaw, float pitch, double reachDistance) {
        HitResult result = raycast(yaw, pitch, reachDistance, true);
        if (result == null || result.getType().equals(HitResult.Type.ENTITY)) return Direction.DOWN;
        return ((BlockHitResult) result).getSide();
    }

    public static BlockPos getRayBlockPos(float yaw, float pitch, double reachDistance) {
        HitResult result = raycast(yaw, pitch, reachDistance, true);
        if (result == null || !result.getType().equals(HitResult.Type.ENTITY)) return null;
        return ((BlockHitResult) result).getBlockPos();
    }

    public static Direction getLookDirection(float yaw, float pitch) {
        Vec3d vec = getRotationVector(yaw, pitch);
        return Direction.getFacing(vec.x, vec.y, vec.z);
    }

    public static Direction getLookDirection(Vec3d rotVec) {
        return Direction.getFacing(rotVec.x, rotVec.y, rotVec.z);
    }

    public static BlockHitResult fixHitResult(BlockHitResult result, BlockPos pos) {
        if (result.getBlockPos().equals(pos)) return result;
        return new BlockHitResult(Vec3d.ofCenter(pos), result.getSide(), pos, result.isInsideBlock());
    }

    public static float getYawByDirection(BlockPos pos, Direction direction) {
        Vec3d position = Vec3d.ofCenter(pos);
        Vec3i vector = direction.getVector();
        double xAdd = 0.5 * vector.getX();
        double yAdd = 0.5 * vector.getY();
        double zAdd = 0.5 * vector.getZ();
        return (float) Rotations.getYaw(position.add(xAdd, yAdd, zAdd));
    }

    public static float getPitchByDirection(BlockPos pos, Direction direction) {
        Vec3d position = Vec3d.ofCenter(pos);
        Vec3i vector = direction.getVector();
        double xAdd = 0.5 * vector.getX();
        double yAdd = 0.5 * vector.getY();
        double zAdd = 0.5 * vector.getZ();
        return (float) Rotations.getPitch(position.add(xAdd, yAdd, zAdd));
    }

    public static float[] getRotationByDirection(BlockPos pos, Direction direction) {
        return new float[]{getYawByDirection(pos, direction), getPitchByDirection(pos, direction)};
    }

    public static Vec3d getRotationVector(float yaw, float pitch) {
        float pitchDegrees = pitch * 0.017453292F;
        float yawDegrees = -yaw * 0.017453292F;
        float yawCos = MathHelper.cos(yawDegrees);
        float yawSin = MathHelper.sin(yawDegrees);
        float pitchCos = MathHelper.cos(pitchDegrees);
        float pitchSin = MathHelper.sin(pitchDegrees);
        return new Vec3d(yawSin * pitchCos, -pitchSin, yawCos * pitchCos);
    }
}
