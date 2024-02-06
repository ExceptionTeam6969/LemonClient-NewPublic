package dev.lemonclient.gui.themes.defaulttheme;

import dev.lemonclient.gui.renderer.GuiRenderer;
import dev.lemonclient.gui.utils.BaseWidget;
import dev.lemonclient.gui.widgets.WWidget;
import dev.lemonclient.utils.render.color.Color;

public interface LCGuiWidget extends BaseWidget {
    default LCGuiTheme theme() {
        return (LCGuiTheme) getTheme();
    }

    default void renderBackground(GuiRenderer renderer, WWidget widget, boolean pressed, boolean mouseOver) {
        LCGuiTheme theme = theme();
        int r = theme.roundAmount();
        double s = theme.scale(2);
        Color outlineColor = theme.outlineColor.get(pressed, mouseOver);

        renderer.quadRounded(widget.x + s, widget.y + s, widget.width - s * 2, widget.height - s * 2, theme.backgroundColor.get(pressed, mouseOver), r - s);
        renderer.quadOutlineRounded(widget, outlineColor, r, s);
    }
}
