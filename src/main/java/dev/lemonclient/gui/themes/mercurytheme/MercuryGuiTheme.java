package dev.lemonclient.gui.themes.mercurytheme;

import dev.lemonclient.gui.DefaultSettingsWidgetFactory;
import dev.lemonclient.gui.themes.defaulttheme.LCGuiTheme;
import dev.lemonclient.gui.themes.mercurytheme.mercury.WMercuryModule;
import dev.lemonclient.gui.themes.mercurytheme.mercury.WMercuryWindow;
import dev.lemonclient.gui.widgets.WWidget;
import dev.lemonclient.gui.widgets.containers.WWindow;
import dev.lemonclient.settings.IntSetting;
import dev.lemonclient.settings.Setting;
import dev.lemonclient.systems.modules.Module;
import dev.lemonclient.utils.render.color.SettingColor;

public class MercuryGuiTheme extends LCGuiTheme {
    @Override
    public WWidget module(Module module) {
        return w(new WMercuryModule(module));
    }

    @Override
    public WWindow window(WWidget icon, String title) {
        return w(new WMercuryWindow(icon, title));
    }

    public final Setting<Integer> moduleWidth = sgGeneral.add(new IntSetting.Builder()
        .name("Module Width")
        .description(".")
        .defaultValue(65)
        .min(0)
        .sliderMax(100)
        .build()
    );

    public MercuryGuiTheme() {
        settingsFactory = new DefaultSettingsWidgetFactory(this);

        backgroundColor.get().set(new SettingColor(30, 30, 30, 181));
    }

    @Override
    public SettingColor getAccentColor() {
        return new SettingColor(73, 73, 73, 255);
    }

    @Override
    public SettingColor getPlaceHolderColor() {
        return new SettingColor(33, 173, 169, 255);
    }

    @Override
    public SettingColor getModuleBackgroundColor() {
        return new SettingColor(10, 10, 10, 108);
    }
}
