package dev.lemonclient.utils.render;

import net.minecraft.util.math.Vec3d;

public class BezierCurve {
    public double A;
    public double B;
    public double C;
    public double D;

    public float percent = 100;
    public long oldTime = System.currentTimeMillis();

    public BezierCurve(double point1, double point2, double point3, double point4) {
        this.A = point1;
        this.B = point2;
        this.C = point3;
        this.D = point4;
    }

    public double interpolateCubic(double percent) {
        return 1 - (A * Math.pow(1 - percent, 3) + 3 * B * Math.pow(1 - percent, 2) * percent + 3 * C * (1 - percent) * Math.pow(percent, 2) + D * Math.pow(percent, 3)) + (A * (1 - percent));
    }

    public double interpolateQuadratic(double x) {
        return A * Math.pow(1 - x, 2) + B * 2 * (1 - x) * x + C * Math.pow(x, 2);
    }

    public double interpolate(double x) {
        return (1 - (B + 0.5 * x * (C - A + x * (2 * A - 5 * B + 4 * C - D + x * (3 * (B - C) + D - A)))) * 4) * (1 / .6);
    }

    public double get(boolean backwards, int iterations) {
        //.25f,.1f,.25f,1f
        //double point75 =  (point1*0.3d);
        //(float) Math.abs(((stepAndInterpolate(backwards, iterations)-point75)-point75-point2)*(1/(point75+point2)))
        //return (float) Math.abs(((stepAndInterpolate(backwards, iterations)-0.075)-0.175)*(1/0.175));
        return Math.max(0, Math.abs(((stepAndInterpolate(backwards, iterations)))));
    }

    public double stepAndInterpolate(boolean backwards, int iterations) {
        if (backwards) {
            for (int x = 0; x < iterations; x++) {
                if (percent < 100) {
                    percent++;
                }
            }
        } else {
            for (int x = 0; x < iterations; x++) {

                if (percent > 0) {
                    percent--;
                }
            }
        }
        return interpolate(percent / 100d);
    }

    public static Vec3d calculatePoint(Vec3d p0, Vec3d p1, Vec3d p2, float t) {
        float u = 1 - t;
        float tt = t * t;
        float uu = u * u;
        float uuu = uu * u;
        float ttt = tt * t;
        return new Vec3d(
            uuu * p0.x + 3 * uu * t * p1.x + 3 * u * tt * p2.x + ttt * p2.x,
            uuu * p0.y + 3 * uu * t * p1.y + 3 * u * tt * p2.y + ttt * p2.y,
            uuu * p0.z + 3 * uu * t * p1.z + 3 * u * tt * p2.z + ttt * p2.z
        );
    }
}
