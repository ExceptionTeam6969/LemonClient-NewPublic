package dev.lemonclient.render.gui;

import dev.lemonclient.utils.render.color.Color;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;

public class ColorBox extends Box {
    public Color color;

    public ColorBox(double x, double y, double width, double height, Color color) {
        super(x, y, width, height);
        this.color = color;
    }

    @Override
    public void render(GuiRenderer renderer, DrawContext context, MatrixStack matrices, double mouseX, double mouseY, float tickDelta) {
        super.render(renderer, context, matrices, mouseX, mouseY, tickDelta);
        renderer.drawRect(x, y, width, height, color);
    }
}
