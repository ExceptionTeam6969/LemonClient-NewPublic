package dev.lemonclient.events.render;

import dev.lemonclient.renderer.Renderer3D;
import dev.lemonclient.utils.Utils;
import net.minecraft.client.util.math.MatrixStack;

public class Render3DEvent {
    private static final Render3DEvent INSTANCE = new Render3DEvent();

    public MatrixStack matrices;
    public Renderer3D renderer;
    public double frameTime;
    public float tickDelta;
    public double offsetX, offsetY, offsetZ;

    public static Render3DEvent get(MatrixStack matrices, Renderer3D renderer, float tickDelta, double offsetX, double offsetY, double offsetZ) {
        INSTANCE.matrices = matrices;
        INSTANCE.renderer = renderer;
        INSTANCE.frameTime = Utils.frameTime;
        INSTANCE.tickDelta = tickDelta;
        INSTANCE.offsetX = offsetX;
        INSTANCE.offsetY = offsetY;
        INSTANCE.offsetZ = offsetZ;
        return INSTANCE;
    }

    public static class Unlimited {
        private static final Render3DEvent.Unlimited INSTANCE = new Render3DEvent.Unlimited();

        public MatrixStack matrices;
        public double frameTime;
        public float tickDelta;

        public static Render3DEvent.Unlimited get(MatrixStack matrices, float tickDelta) {
            INSTANCE.matrices = matrices;
            INSTANCE.frameTime = Utils.frameTime;
            INSTANCE.tickDelta = tickDelta;
            return INSTANCE;
        }
    }
}
