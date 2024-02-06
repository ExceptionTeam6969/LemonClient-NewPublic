package dev.lemonclient.render.gui;

import dev.lemonclient.utils.render.color.Color;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;

public class TextBox extends ColorBox {
    public String text;
    public double fontScale = 1.0;

    public TextBox(String text, double x, double y, double width, double height, Color color) {
        super(x, y, width, height, color);
        this.text = text;
    }

    @Override
    public void render(GuiRenderer renderer, DrawContext context, MatrixStack matrices, double mouseX, double mouseY, float tickDelta) {
        renderer.centerText(text, x, y, width, height, fontScale, color);
        super.render(renderer, context, matrices, mouseX, mouseY, tickDelta);
    }
}
