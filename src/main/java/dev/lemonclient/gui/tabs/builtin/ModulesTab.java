package dev.lemonclient.gui.tabs.builtin;

import dev.lemonclient.gui.GuiTheme;
import dev.lemonclient.gui.GuiThemes;
import dev.lemonclient.gui.tabs.Tab;
import dev.lemonclient.gui.tabs.TabScreen;
import net.minecraft.client.gui.screen.Screen;

public class ModulesTab extends Tab {
    public ModulesTab() {
        super("Modules");
    }

    @Override
    public TabScreen createScreen(GuiTheme theme) {
        return theme.modulesScreen();
    }

    @Override
    public boolean isScreen(Screen screen) {
        return GuiThemes.get().isModulesScreen(screen);
    }
}
