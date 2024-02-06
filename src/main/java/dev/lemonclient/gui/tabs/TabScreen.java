package dev.lemonclient.gui.tabs;

import dev.lemonclient.gui.GuiTheme;
import dev.lemonclient.gui.WidgetScreen;
import dev.lemonclient.gui.utils.Cell;
import dev.lemonclient.gui.widgets.WWidget;

public abstract class TabScreen extends WidgetScreen {
    public final Tab tab;

    public TabScreen(GuiTheme theme, Tab tab) {
        super(theme, tab.name);

        this.tab = tab;
    }

    public <T extends WWidget> Cell<T> addDirect(T widget) {
        return super.add(widget);
    }
}
