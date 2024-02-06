package dev.lemonclient.render.canvas;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;

public class MatrixCanvas {
    protected MinecraftClient mc = MinecraftClient.getInstance();
    protected DrawContext drawContext;
    protected MatrixStack matrix = RenderSystem.getModelViewStack();
    protected MatrixStack savedMatrix;

    public void backupMatrixStack() {
        savedMatrix = getMatrixStack();
    }

    public void restoreMatrixStack() {
        setMatrix(savedMatrix);
    }

    public MatrixStack getMatrixStack() {
        return matrix;
    }

    public void setMatrix(MatrixStack matrix) {
        this.matrix = matrix;
    }

    public DrawContext getDrawContext() {
        return drawContext;
    }

    public void setDrawContext(DrawContext drawContext) {
        this.drawContext = drawContext;
    }
}
