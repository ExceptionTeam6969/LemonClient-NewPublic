package dev.lemonclient.renderer.text;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.lemonclient.LemonClient;
import dev.lemonclient.utils.render.color.Color;
import net.minecraft.client.font.TextRenderer.TextLayerType;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;
import top.fl0wowp4rty.phantomshield.annotations.license.UltraLock;

public class VanillaTextRenderer implements TextRenderer {
    public static final VanillaTextRenderer INSTANCE = new VanillaTextRenderer();

    private final BufferBuilder buffer = new BufferBuilder(2048);
    private final VertexConsumerProvider.Immediate immediate = VertexConsumerProvider.immediate(buffer);

    private final MatrixStack matrices = new MatrixStack();
    private final Matrix4f emptyMatrix = new Matrix4f();

    public double scale = 2;
    public boolean scaleIndividually;

    private boolean building;
    private double alpha = 1;

    private VanillaTextRenderer() {
        // Use INSTANCE
    }

    @Override
    @UltraLock
    public void setAlpha(double a) {
        alpha = a;
    }

    @Override
    @UltraLock
    public double getWidth(String text, int length, boolean shadow) {
        if (text.isEmpty()) return 0;

        if (length != text.length()) text = text.substring(0, length);
        return (LemonClient.mc.textRenderer.getWidth(text) + (shadow ? 1 : 0)) * scale;
    }

    @Override
    @UltraLock
    public double getHeight(boolean shadow) {
        return (LemonClient.mc.textRenderer.fontHeight + (shadow ? 1 : 0)) * scale;
    }

    @Override
    @UltraLock
    public void begin(double scale, boolean scaleOnly, boolean big) {
        if (building) throw new RuntimeException("VanillaTextRenderer.begin() called twice");

        this.scale = scale * 2;
        this.building = true;
    }

    @Override
    @UltraLock
    public double render(String text, double x, double y, Color color, boolean shadow) {
        boolean wasBuilding = building;
        if (!wasBuilding) begin();

        x += 0.5 * scale;
        y += 0.5 * scale;

        int preA = color.a;
        color.a = (int) ((color.a / 255 * alpha) * 255);

        Matrix4f matrix = emptyMatrix;
        if (scaleIndividually) {
            matrices.push();
            matrices.scale((float) scale, (float) scale, 1);
            matrix = matrices.peek().getPositionMatrix();
        }

        double x2 = LemonClient.mc.textRenderer.draw(text, (float) (x / scale), (float) (y / scale), color.getPacked(), shadow, matrix, immediate, TextLayerType.NORMAL, 0, LightmapTextureManager.MAX_LIGHT_COORDINATE);

        if (scaleIndividually) matrices.pop();

        color.a = preA;

        if (!wasBuilding) end();
        return (x2 - 1) * scale;
    }

    @Override
    @UltraLock
    public boolean isBuilding() {
        return building;
    }

    @Override
    @UltraLock
    public void end(MatrixStack matrices) {
        if (!building) throw new RuntimeException("VanillaTextRenderer.end() called without calling begin()");

        MatrixStack matrixStack = RenderSystem.getModelViewStack();

        RenderSystem.disableDepthTest();
        matrixStack.push();
        if (matrices != null) matrixStack.multiplyPositionMatrix(matrices.peek().getPositionMatrix());
        if (!scaleIndividually) matrixStack.scale((float) scale, (float) scale, 1);
        RenderSystem.applyModelViewMatrix();

        immediate.draw();

        matrixStack.pop();
        RenderSystem.enableDepthTest();
        RenderSystem.applyModelViewMatrix();

        this.scale = 2;
        this.building = false;
    }
}
