package dev.lemonclient.gui.tabs.builtin;

import dev.lemonclient.gui.GuiTheme;
import dev.lemonclient.gui.GuiThemes;
import dev.lemonclient.gui.tabs.Tab;
import dev.lemonclient.gui.tabs.TabScreen;
import dev.lemonclient.gui.tabs.WindowTabScreen;
import dev.lemonclient.gui.widgets.containers.WTable;
import dev.lemonclient.gui.widgets.input.WDropdown;
import dev.lemonclient.utils.misc.NbtUtils;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.nbt.NbtCompound;

import static dev.lemonclient.LemonClient.mc;

public class GuiTab extends Tab {
    public GuiTab() {
        super("GUI");
    }

    @Override
    public TabScreen createScreen(GuiTheme theme) {
        return new GuiScreen(theme, this);
    }

    @Override
    public boolean isScreen(Screen screen) {
        return screen instanceof GuiScreen;
    }

    private static class GuiScreen extends WindowTabScreen {
        public GuiScreen(GuiTheme theme, Tab tab) {
            super(theme, tab);

            theme.settings.onActivated();
        }

        @Override
        public void initWidgets() {
            WTable table = add(theme.table()).expandX().widget();

            table.add(theme.label("Theme:"));
            WDropdown<String> themeW = table.add(theme.dropdown(GuiThemes.getNames(), GuiThemes.get().name)).widget();
            themeW.action = () -> {
                GuiThemes.select(themeW.get());

                mc.setScreen(null);
                tab.openScreen(GuiThemes.get());
            };

            add(theme.settings(theme.settings)).expandX();
        }

        @Override
        public boolean toClipboard() {
            return NbtUtils.toClipboard(theme.name + " GUI Theme", theme.toTag());
        }

        @Override
        public boolean fromClipboard() {
            NbtCompound clipboard = NbtUtils.fromClipboard(theme.toTag());

            if (clipboard != null) {
                theme.fromTag(clipboard);
                return true;
            }

            return false;
        }
    }
}
