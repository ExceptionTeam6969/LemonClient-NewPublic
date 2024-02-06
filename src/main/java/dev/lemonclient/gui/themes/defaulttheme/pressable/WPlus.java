package dev.lemonclient.gui.themes.defaulttheme.pressable;

import dev.lemonclient.gui.renderer.GuiRenderer;
import dev.lemonclient.gui.themes.defaulttheme.LCGuiTheme;
import dev.lemonclient.gui.themes.defaulttheme.LCGuiWidget;

public class WPlus extends dev.lemonclient.gui.widgets.pressable.WPlus implements LCGuiWidget {
    @Override
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        LCGuiTheme theme = theme();
        double pad = pad();
        double s = theme.scale(3);

        renderBackground(renderer, this, pressed, mouseOver);
        renderer.quad(x + pad, y + height / 2 - s / 2, width - pad * 2, s, theme.plusColor.get());
        renderer.quad(x + width / 2 - s / 2, y + pad, s, height - pad * 2, theme.plusColor.get());
    }
}
