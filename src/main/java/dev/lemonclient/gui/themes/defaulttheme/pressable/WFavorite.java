package dev.lemonclient.gui.themes.defaulttheme.pressable;

import dev.lemonclient.gui.themes.defaulttheme.LCGuiWidget;
import dev.lemonclient.utils.render.color.Color;

public class WFavorite extends dev.lemonclient.gui.widgets.pressable.WFavorite implements LCGuiWidget {
    public WFavorite(boolean checked) {
        super(checked);
    }

    @Override
    protected Color getColor() {
        return theme().favoriteColor.get();
    }
}
