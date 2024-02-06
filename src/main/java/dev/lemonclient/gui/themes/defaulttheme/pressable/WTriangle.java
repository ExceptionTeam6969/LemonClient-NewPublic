package dev.lemonclient.gui.themes.defaulttheme.pressable;

import dev.lemonclient.gui.renderer.GuiRenderer;
import dev.lemonclient.gui.themes.defaulttheme.LCGuiWidget;

public class WTriangle extends dev.lemonclient.gui.widgets.pressable.WTriangle implements LCGuiWidget {
    @Override
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        renderer.rotatedQuad(x, y, width, height, rotation, GuiRenderer.TRIANGLE, theme().backgroundColor.get(pressed, mouseOver));
    }
}
