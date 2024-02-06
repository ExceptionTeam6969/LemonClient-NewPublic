package dev.lemonclient.render.canvas;

import dev.lemonclient.render.CanvasManager;
import dev.lemonclient.utils.render.color.Color;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Vector2d;

public class VanillaTextCanvas extends TextCanvas {
    public TextRenderer font = mc.textRenderer;

    public static VanillaTextCanvas CURRENT;

    private VanillaTextCanvas() {
    }

    public static VanillaTextCanvas newCanvas() {
        VanillaTextCanvas canvas = new VanillaTextCanvas();
        CanvasManager.canvas.add(canvas);
        CURRENT = canvas;
        return canvas;
    }

    @Override
    public void draw(String text, double x, double y, Color color, boolean shadow) {
        drawContext.drawText(font, text, (int) x, (int) y, color.getPacked(), shadow);
    }

    @Override
    public void drawCenter(String text, double x, double y, Color color, boolean shadow) {
        Vector2d center = calcCenter(this, text, x, y, shadow);
        x = center.x;
        this.draw(text, x, y, color, shadow);
    }

    @Override
    public void drawRectCenter(String text, double x, double y, double width, double height, Color color, boolean shadow) {
        Vector2d center = calcCenter(this, text, x, y, shadow);
        x = center.x;
        y = center.y;
        this.draw(text, x, y, color, shadow);
    }

    @Override
    public double width(String text, boolean shadow) {
        return width(text, this.scale);
    }

    @Override
    public double height(String text, boolean shadow) {
        return height(text, this.scale);
    }

    private void draw(String message, float x, float y, double scale, Color color, boolean shadow) {
        MatrixStack matrices = drawContext.getMatrices();
        matrices.push();
        x += 0.5 * scale;
        y += 0.5 * scale;
        matrices.scale((float) scale, (float) scale, 1);
        drawContext.drawText(mc.textRenderer, message, (int) (x / scale), (int) (y / scale), color.getPacked(), shadow);
        matrices.pop();
    }

    private double width(String message, double scale) {
        return mc.textRenderer.getWidth(message) * scale;
    }

    private double height(String message, double scale) {
        return mc.textRenderer.getWidth(message) * scale;
    }

    @Override
    public void scale(double next) {
        this.scale = next;
    }

    @Override
    public void begin() {
    }

    @Override
    public void end() {
    }
}
