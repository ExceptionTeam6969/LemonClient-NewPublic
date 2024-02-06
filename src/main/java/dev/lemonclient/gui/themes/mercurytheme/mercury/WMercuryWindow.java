package dev.lemonclient.gui.themes.mercurytheme.mercury;

import dev.lemonclient.gui.renderer.GuiRenderer;
import dev.lemonclient.gui.themes.defaulttheme.LCGuiWidget;
import dev.lemonclient.gui.widgets.WWidget;
import dev.lemonclient.gui.widgets.containers.WWindow;

public class WMercuryWindow extends WWindow implements LCGuiWidget {
    public WMercuryWindow(WWidget icon, String title) {
        super(icon, title);
    }

    @Override
    protected WHeader header(WWidget icon) {
        return new WMeteorHeader(icon);
    }

    @Override
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        if (expanded || animProgress > 0) {
            renderer.quad(x + 5, y + header.height, width - 10, height - header.height, theme().backgroundColor.get());
        }
    }

    private class WMeteorHeader extends WHeader {
        public WMeteorHeader(WWidget icon) {
            super(icon);
        }

        @Override
        protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
            renderer.quadRounded(this, theme().accentColor.get(), theme().round.get());
        }
    }
}
