/*
 * This file is modified from the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package dev.lemonclient.gui.themes.bozetheme.boze;

import dev.lemonclient.gui.renderer.GuiRenderer;
import dev.lemonclient.gui.themes.bozetheme.BozeGuiTheme;
import dev.lemonclient.gui.utils.BaseWidget;
import dev.lemonclient.gui.widgets.WWidget;
import dev.lemonclient.utils.render.color.Color;

public interface BozeWidget extends BaseWidget {
    default BozeGuiTheme theme() {
        return (BozeGuiTheme) getTheme();
    }

    default void renderBackground(GuiRenderer renderer, WWidget widget, boolean pressed, boolean mouseOver) {
        BozeGuiTheme theme = theme();
        double s = theme.scale(2);

        renderer.quad(widget.x + s, widget.y + s, widget.width - s * 2, widget.height - s * 2, theme.backgroundColor.get(pressed, mouseOver));

        Color outlineColor = theme.outlineColor.get(pressed, mouseOver);
        renderer.quad(widget.x, widget.y, widget.width, s, outlineColor);
        renderer.quad(widget.x, widget.y + widget.height - s, widget.width, s, outlineColor);
        renderer.quad(widget.x, widget.y + s, s, widget.height - s * 2, outlineColor);
        renderer.quad(widget.x + widget.width - s, widget.y + s, s, widget.height - s * 2, outlineColor);
    }
}
