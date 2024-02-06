package dev.lemonclient.gui.utils;

import dev.lemonclient.gui.GuiTheme;
import dev.lemonclient.gui.WidgetScreen;

public interface IScreenFactory {
    WidgetScreen createScreen(GuiTheme theme);
}
