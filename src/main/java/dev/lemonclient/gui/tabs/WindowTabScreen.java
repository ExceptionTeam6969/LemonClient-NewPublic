package dev.lemonclient.gui.tabs;

import dev.lemonclient.gui.GuiTheme;
import dev.lemonclient.gui.utils.Cell;
import dev.lemonclient.gui.widgets.WWidget;
import dev.lemonclient.gui.widgets.containers.WWindow;

public abstract class WindowTabScreen extends TabScreen {
    protected final WWindow window;

    public WindowTabScreen(GuiTheme theme, Tab tab) {
        super(theme, tab);

        window = super.add(theme.window(tab.name)).center().widget();
    }

    @Override
    public <W extends WWidget> Cell<W> add(W widget) {
        return window.add(widget);
    }

    @Override
    public void clear() {
        window.clear();
    }
}
