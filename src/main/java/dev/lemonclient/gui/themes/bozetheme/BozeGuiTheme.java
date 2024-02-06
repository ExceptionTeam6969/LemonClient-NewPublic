package dev.lemonclient.gui.themes.bozetheme;

import dev.lemonclient.gui.DefaultSettingsWidgetFactory;
import dev.lemonclient.gui.themes.bozetheme.boze.WBozeModule;
import dev.lemonclient.gui.themes.bozetheme.boze.WBozeWindow;
import dev.lemonclient.gui.themes.defaulttheme.LCGuiTheme;
import dev.lemonclient.gui.utils.AlignmentX;
import dev.lemonclient.gui.widgets.WWidget;
import dev.lemonclient.gui.widgets.containers.WWindow;
import dev.lemonclient.settings.Setting;
import dev.lemonclient.systems.modules.Module;
import dev.lemonclient.utils.render.color.SettingColor;

import java.awt.*;

public class BozeGuiTheme extends LCGuiTheme {
    @Override
    public WWidget module(Module module) {
        return w(new WBozeModule(module));
    }

    @Override
    public WWindow window(WWidget icon, String title) {
        return w(new WBozeWindow(icon, title));
    }

    public BozeGuiTheme() {
        settingsFactory = new DefaultSettingsWidgetFactory(this);

        backgroundColor.get().set(new SettingColor(new Color(25, 25, 25, 255)));
    }

    @Override
    public Setting<AlignmentX> addModuleAlignmentSetting() {
        return null;
    }

    @Override
    public SettingColor getAccentColor() {
        return new SettingColor(25, 25, 25, 255);
    }

    @Override
    public SettingColor getPlaceHolderColor() {
        return new SettingColor(44, 44, 44, 255);
    }

    @Override
    public SettingColor getModuleBackgroundColor() {
        return new SettingColor(149, 123, 214, 255);
    }
}
