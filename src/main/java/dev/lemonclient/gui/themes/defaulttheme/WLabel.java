package dev.lemonclient.gui.themes.defaulttheme;

import dev.lemonclient.gui.renderer.GuiRenderer;

public class WLabel extends dev.lemonclient.gui.widgets.WLabel implements LCGuiWidget {
    public WLabel(String text, boolean title) {
        super(text, title);
    }

    @Override
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        if (!text.isEmpty()) {
            renderer.text(text, x, y, color != null ? color : (title ? theme().titleTextColor.get() : theme().textColor.get()), title);
        }
    }
}
