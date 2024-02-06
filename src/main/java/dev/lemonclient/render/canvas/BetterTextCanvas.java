package dev.lemonclient.render.canvas;

import dev.lemonclient.render.CanvasManager;
import dev.lemonclient.renderer.text.TextRenderer;
import dev.lemonclient.utils.render.color.Color;
import org.joml.Vector2d;

public class BetterTextCanvas extends TextCanvas {
    public TextRenderer font = TextRenderer.get();
    private boolean building;

    public static BetterTextCanvas CURRENT;

    private BetterTextCanvas() {
    }

    public static BetterTextCanvas newCanvas() {
        BetterTextCanvas canvas = new BetterTextCanvas();
        CanvasManager.canvas.add(canvas);
        CURRENT = canvas;
        return canvas;
    }

    @Override
    public void draw(String text, double x, double y, Color color, boolean shadow) {
        begin();
        font.render(text, x, y, color, shadow);
        end();
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
        double w;
        begin();
        w = font.getWidth(text, shadow);
        end();
        return w;
    }

    @Override
    public double height(String text, boolean shadow) {
        double h;
        begin();
        h = font.getHeight(shadow);
        end();
        return h;
    }

    @Override
    public void scale(double next) {
        this.scale = next;
    }

    @Override
    public void begin() {
        if (!building) {
            font.begin(scale);
            building = true;
        }
    }

    @Override
    public void end() {
        if (building) {
            font.end(getMatrixStack());
            building = false;
        }
    }
}
