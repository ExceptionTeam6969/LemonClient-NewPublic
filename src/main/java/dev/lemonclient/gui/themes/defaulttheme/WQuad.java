package dev.lemonclient.gui.themes.defaulttheme;

import dev.lemonclient.gui.renderer.GuiRenderer;
import dev.lemonclient.utils.render.color.Color;

public class WQuad extends dev.lemonclient.gui.widgets.WQuad {
    public WQuad(Color color) {
        super(color);
    }

    @Override
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        renderer.quadRounded(x, y, width, height, color, ((LCGuiTheme) theme).roundAmount());
    }
}
