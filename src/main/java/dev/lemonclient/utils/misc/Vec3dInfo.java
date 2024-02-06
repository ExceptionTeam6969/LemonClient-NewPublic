package dev.lemonclient.utils.misc;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import static dev.lemonclient.LemonClient.mc;

public class Vec3dInfo {

    public static Vec3d getEyeVec(PlayerEntity entity) {
        return entity.getPos().add(0, entity.getEyeHeight(entity.getPose()), 0);
    }

    public static Vec3d closestVec3d(BlockPos blockPos) {
        if (blockPos == null) return new Vec3d(0.0, 0.0, 0.0);
        double x = MathHelper.clamp((mc.player.getX() - blockPos.getX()), 0.0, 1.0);
        double y = MathHelper.clamp((mc.player.getY() - blockPos.getY()), 0.0, 0.6);
        double z = MathHelper.clamp((mc.player.getZ() - blockPos.getZ()), 0.0, 1.0);
        return new Vec3d(blockPos.getX() + x, blockPos.getY() + y, blockPos.getZ() + z);
    }
}
