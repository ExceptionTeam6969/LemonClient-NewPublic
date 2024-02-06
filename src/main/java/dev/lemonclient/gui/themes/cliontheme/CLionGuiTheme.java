package dev.lemonclient.gui.themes.cliontheme;

import dev.lemonclient.gui.DefaultSettingsWidgetFactory;
import dev.lemonclient.gui.themes.cliontheme.clion.WCLionModule;
import dev.lemonclient.gui.themes.cliontheme.clion.WCLionWindow;
import dev.lemonclient.gui.themes.defaulttheme.LCGuiTheme;
import dev.lemonclient.gui.utils.AlignmentX;
import dev.lemonclient.gui.widgets.WWidget;
import dev.lemonclient.gui.widgets.containers.WWindow;
import dev.lemonclient.settings.Setting;
import dev.lemonclient.systems.modules.Module;
import dev.lemonclient.utils.render.color.SettingColor;

import java.awt.*;

public class CLionGuiTheme extends LCGuiTheme {
    @Override
    public WWidget module(Module module) {
        return w(new WCLionModule(module));
    }

    @Override
    public WWindow window(WWidget icon, String title) {
        return w(new WCLionWindow(icon, title));
    }


    public CLionGuiTheme() {
        settingsFactory = new DefaultSettingsWidgetFactory(this);
        backgroundColor.get().set(new SettingColor(new Color(35, 34, 34, 255)));
    }

    @Override
    public Setting<AlignmentX> addModuleAlignmentSetting() {
        return null;
    }

    @Override
    public SettingColor getAccentColor() {
        return new SettingColor(21, 21, 21, 255);
    }

    @Override
    public SettingColor getModuleBackgroundColor() {
        return new SettingColor(251, 129, 187, 255);
    }

    @Override
    public SettingColor getPlaceHolderColor() {
        return new SettingColor(33, 173, 169, 255);
    }
}
