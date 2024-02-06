package dev.lemonclient.gui.themes.defaulttheme;

import dev.lemonclient.gui.renderer.GuiRenderer;
import dev.lemonclient.gui.widgets.WWidget;

public class WWindow extends dev.lemonclient.gui.widgets.containers.WWindow implements LCGuiWidget {
    public WWindow(WWidget icon, String title) {
        super(icon, title);
    }

    @Override
    protected WHeader header(WWidget icon) {
        return new WMeteorHeader(icon);
    }

    @Override
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        if (expanded || animProgress > 0) {
            renderer.quadRounded(x, y + header.height / 2, width, height - header.height / 2, theme().backgroundColor.get(), ((LCGuiTheme) theme).roundAmount(), false);
        }
    }

    private class WMeteorHeader extends WHeader {
        public WMeteorHeader(WWidget icon) {
            super(icon);
        }

        @Override
        protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
            renderer.quadRounded(this, theme().accentColor.get(), ((LCGuiTheme) theme).roundAmount());
        }
    }
}
