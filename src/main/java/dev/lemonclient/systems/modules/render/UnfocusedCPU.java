package dev.lemonclient.systems.modules.render;

import dev.lemonclient.settings.IntSetting;
import dev.lemonclient.settings.Setting;
import dev.lemonclient.settings.SettingGroup;
import dev.lemonclient.systems.modules.Categories;
import dev.lemonclient.systems.modules.Module;

public class UnfocusedCPU extends Module {
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();

    public final Setting<Integer> fps = sgGeneral.add(new IntSetting.Builder()
        .name("Target Fps")
        .description("Target FPS to set as the limit when the window is not focused.")
        .min(1)
        .defaultValue(1)
        .sliderRange(1, 20)
        .build()
    );

    public UnfocusedCPU() {
        super(Categories.Render, "Unfocused Cpu", "Limits FPS when your Minecraft window is not focused.");
    }
}
