package dev.lemonclient.utils.player;

import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;

import static dev.lemonclient.LemonClient.mc;

public class RotationUtils {
    public static float nextYaw(double current, double target, double step) {
        double i = yawAngle(current, target);

        if (step >= Math.abs(i)) {
            return (float) (current + i);
        } else {
            return (float) (current + (i < 0 ? -1 : 1) * step);
        }
    }

    public static float getAngleDifference(final float a, final float b) {
        return ((((a - b) % 360F) + 540F) % 360F) - 180F;
    }

    public static float[] fixedSensitivity(float[] currentDir, float[] lastDir, double sensitivity) {
        float f = (float) (sensitivity * 0.6000000238418579 + 0.20000000298023224);
        float gcd = (float) (f * f * f * 1.2);
        // fix yaw
        float deltaYaw = currentDir[0] - lastDir[0];
        deltaYaw -= deltaYaw % gcd;
        currentDir[0] = lastDir[0] + deltaYaw;

        // fix pitch
        float deltaPitch = currentDir[1] - lastDir[1];
        deltaPitch -= deltaPitch % gcd;
        currentDir[1] = lastDir[1] + deltaPitch;
        return currentDir;
    }

    public static double yawAngle(double current, double target) {
        double c = MathHelper.wrapDegrees(current) + 180, t = MathHelper.wrapDegrees(target) + 180;
        if (c > t) {
            return t + 360 - c < Math.abs(c - t) ? 360 - c + t : t - c;
        } else {
            return 360 - t + c < Math.abs(c - t) ? -(360 - t + c) : t - c;
        }
    }

    public static float nextPitch(double current, double target, double step) {
        double i = target - current;

        return (float) (Math.abs(i) <= step ? target : i >= 0 ? current + step : current - step);
    }

    public static double radAngle(Vec2f vec1, Vec2f vec2) {
        double p = vec1.x * vec2.x + vec1.y * vec2.y;
        p /= Math.sqrt(vec1.x * vec1.x + vec1.y * vec1.y);
        p /= Math.sqrt(vec2.x * vec2.x + vec2.y * vec2.y);
        return Math.acos(p);
    }

    // These 2 are from client rotation utils
    public static double getYaw(Vec3d start, Vec3d target) {
        return mc.player.getYaw() + MathHelper.wrapDegrees((float) Math.toDegrees(Math.atan2(target.getZ() - start.getZ(), target.getX() - start.getX())) - 90f - mc.player.getYaw());
    }

    public static double getPitch(Vec3d start, Vec3d target) {
        double diffX = target.getX() - start.getX();
        double diffY = target.getY() - start.getY();
        double diffZ = target.getZ() - start.getZ();

        double diffXZ = Math.sqrt(diffX * diffX + diffZ * diffZ);

        return mc.player.getPitch() + MathHelper.wrapDegrees((float) -Math.toDegrees(Math.atan2(diffY, diffXZ)) - mc.player.getPitch());
    }
}
