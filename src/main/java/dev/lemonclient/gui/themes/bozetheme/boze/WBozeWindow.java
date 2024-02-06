package dev.lemonclient.gui.themes.bozetheme.boze;

import dev.lemonclient.gui.renderer.GuiRenderer;
import dev.lemonclient.gui.themes.defaulttheme.LCGuiTheme;
import dev.lemonclient.gui.themes.defaulttheme.LCGuiWidget;
import dev.lemonclient.gui.widgets.WWidget;
import dev.lemonclient.gui.widgets.containers.WWindow;

public class WBozeWindow extends WWindow implements LCGuiWidget {
    public WBozeWindow(WWidget icon, String title) {
        super(icon, title);
    }

    @Override
    protected WWindow.WHeader header(WWidget icon) {
        return new WHeader(icon);
    }

    @Override
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        if (expanded || animProgress > 0) {
            renderer.quadRounded(x, y + header.height, width, height - header.height, theme().backgroundColor.get(), 8);
        }
    }

    private class WHeader extends WWindow.WHeader {
        public WHeader(WWidget icon) {
            super(icon);
        }

        @Override
        protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
            LCGuiTheme theme = theme();

            renderer.quadRounded(this.x, this.y, this.width, this.height, theme.accentColor.get(), 8, true);
            if (expanded) {
                renderer.quad(this.x, this.y + this.height - 6, this.width, 12, theme.accentColor.get());
            } else {
                renderer.quadRounded(this.x, this.y + this.height - 6, this.width, 12, theme.accentColor.get(), 8);
            }
        }
    }
}
