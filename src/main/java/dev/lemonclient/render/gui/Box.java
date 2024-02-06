package dev.lemonclient.render.gui;

import dev.lemonclient.utils.render.color.Color;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;

public class Box extends Widget {
    public double width;
    public double height;
    public boolean hovered;

    public Box(double x, double y, double width, double height) {
        super(x, y);
        this.width = width;
        this.height = height;
    }

    @Override
    public void render(GuiRenderer renderer, DrawContext context, MatrixStack matrices, double mouseX, double mouseY, float tickDelta) {
        hovered = isMouseHoveringRect(x, y, width, height, mouseX, mouseY);
    }

    @Override
    public void debugRender(GuiRenderer renderer, DrawContext context, MatrixStack matrices, double mouseX, double mouseY, float tickDelta) {
        Color color = moving ? Color.RED : Color.BLACK;
        renderer.drawLine(x, y, x + width, y, color);
        renderer.drawLine(x + width, y, x + width, y + height, color);
        renderer.drawLine(x + width, y + height, x, y + height, color);
        renderer.drawLine(x, y + height, x, y, color);
    }

    public boolean isMouseHoveringRect(double x, double y, double width, double height, double mouseX, double mouseY) {
        return mouseX >= x && mouseY >= y && mouseX <= x + width && mouseY <= y + height;
    }
}
