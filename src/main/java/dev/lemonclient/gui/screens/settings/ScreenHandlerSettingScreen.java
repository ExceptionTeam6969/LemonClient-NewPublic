package dev.lemonclient.gui.screens.settings;

import dev.lemonclient.gui.GuiTheme;
import dev.lemonclient.gui.widgets.WWidget;
import dev.lemonclient.settings.Setting;
import net.minecraft.registry.Registries;
import net.minecraft.screen.ScreenHandlerType;

import java.util.List;

public class ScreenHandlerSettingScreen extends LeftRightListSettingScreen<ScreenHandlerType<?>> {
    public ScreenHandlerSettingScreen(GuiTheme theme, Setting<List<ScreenHandlerType<?>>> setting) {
        super(theme, "Select Screen Handlers", setting, setting.get(), Registries.SCREEN_HANDLER);
    }

    @Override
    protected WWidget getValueWidget(ScreenHandlerType<?> value) {
        return theme.label(getValueName(value));
    }

    @Override
    protected String getValueName(ScreenHandlerType<?> type) {
        return Registries.SCREEN_HANDLER.getId(type).toString();
    }
}
