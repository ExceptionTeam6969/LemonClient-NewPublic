package dev.lemonclient.gui.themes.defaulttheme;

import dev.lemonclient.gui.renderer.GuiRenderer;
import dev.lemonclient.utils.render.color.Color;

public class WMultiLabel extends dev.lemonclient.gui.widgets.WMultiLabel implements LCGuiWidget {
    public WMultiLabel(String text, boolean title, double maxWidth) {
        super(text, title, maxWidth);
    }

    @Override
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        double h = theme.textHeight(title);
        Color color = theme().textColor.get();

        for (int i = 0; i < lines.size(); i++) {
            renderer.text(lines.get(i), x, y + h * i, color, false);
        }
    }
}
