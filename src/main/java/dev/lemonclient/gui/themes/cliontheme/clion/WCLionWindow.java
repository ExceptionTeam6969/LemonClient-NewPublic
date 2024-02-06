package dev.lemonclient.gui.themes.cliontheme.clion;

import dev.lemonclient.gui.renderer.GuiRenderer;
import dev.lemonclient.gui.themes.defaulttheme.LCGuiWidget;
import dev.lemonclient.gui.widgets.WWidget;
import dev.lemonclient.gui.widgets.containers.WWindow;

public class WCLionWindow extends WWindow implements LCGuiWidget {
    public WCLionWindow(WWidget icon, String title) {
        super(icon, title);
    }

    @Override
    protected WHeader header(WWidget icon) {
        return new WMeteorHeader(icon);
    }

    @Override
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        if (expanded || animProgress > 0) {
            renderer.quad(x, y + header.height, width, height - header.height, theme().backgroundColor.get());
        }
    }

    private class WMeteorHeader extends WHeader {
        public WMeteorHeader(WWidget icon) {
            super(icon);
        }

        @Override
        protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
            renderer.quadRounded(this.x - 6, this.y, this.width + 12, this.height + 4, theme().accentColor.get(), 8, true);
        }
    }
}
