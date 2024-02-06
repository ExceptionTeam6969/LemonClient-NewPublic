package dev.lemonclient.render.gui;

import dev.lemonclient.utils.render.color.Color;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;

public class RoundBox extends ColorBox {
    public double radius;

    public RoundBox(double x, double y, double width, double height, double radius, Color color) {
        super(x, y, width, height, color);
        this.radius = radius;
    }

    @Override
    public void render(GuiRenderer renderer, DrawContext context, MatrixStack matrices, double mouseX, double mouseY, float tickDelta) {
        super.render(renderer, context, matrices, mouseX, mouseY, tickDelta);
    }

    @Override
    public void debugRender(GuiRenderer renderer, DrawContext context, MatrixStack matrices, double mouseX, double mouseY, float tickDelta) {
        super.debugRender(renderer, context, matrices, mouseX, mouseY, tickDelta);
        Color rdColor = Color.BLUE;
        double rdX = this.x + radius;
        double rdY = this.y + radius;
        double rdWidth = this.width - radius * 2;
        double rdHeight = this.height - radius * 2;
        renderer.drawLine(rdX, rdY, rdX + rdWidth, rdY, rdColor);
        renderer.drawLine(rdX + rdWidth, rdY, rdX + rdWidth, rdY + rdHeight, rdColor);
        renderer.drawLine(rdX + rdWidth, rdY + rdHeight, rdX, rdY + rdHeight, rdColor);
        renderer.drawLine(rdX, rdY + rdHeight, rdX, rdY, rdColor);
    }
}
