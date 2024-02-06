package dev.lemonclient.gui.themes.defaulttheme.pressable;

import dev.lemonclient.gui.renderer.GuiRenderer;
import dev.lemonclient.gui.renderer.packer.GuiTexture;
import dev.lemonclient.gui.themes.defaulttheme.LCGuiTheme;
import dev.lemonclient.gui.themes.defaulttheme.LCGuiWidget;

public class WButton extends dev.lemonclient.gui.widgets.pressable.WButton implements LCGuiWidget {
    public WButton(String text, GuiTexture texture) {
        super(text, texture);
    }

    @Override
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        LCGuiTheme theme = theme();
        double pad = pad();

        renderBackground(renderer, this, pressed, mouseOver);

        if (text != null) {
            renderer.text(text, x + width / 2 - textWidth / 2, y + pad, theme.textColor.get(), false);
        } else {
            double ts = theme.textHeight();
            renderer.quad(x + width / 2 - ts / 2, y + pad, ts, ts, texture, theme.textColor.get());
        }
    }
}
