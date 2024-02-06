package dev.lemonclient.systems.modules.render;

import dev.lemonclient.settings.Setting;
import dev.lemonclient.settings.SettingGroup;
import dev.lemonclient.systems.modules.Categories;
import dev.lemonclient.systems.modules.Module;

public class SwingAnimation extends Module {
    public SwingAnimation() {
        super(Categories.Render, "Swing Animation", "Modifies swing rendering.");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    //--------------------General--------------------//
    public final Setting<Integer> swingSpeed = intSetting(sgGeneral, "Swing Speed", 6, 1, 50);
}
