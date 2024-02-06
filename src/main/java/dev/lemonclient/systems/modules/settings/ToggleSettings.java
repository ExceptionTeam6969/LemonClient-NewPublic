package dev.lemonclient.systems.modules.settings;

import dev.lemonclient.settings.BoolSetting;
import dev.lemonclient.settings.DoubleSetting;
import dev.lemonclient.settings.Setting;
import dev.lemonclient.settings.SettingGroup;
import dev.lemonclient.systems.modules.Categories;
import dev.lemonclient.systems.modules.Module;

public class ToggleSettings extends Module {
    public ToggleSettings() {
        super(Categories.Settings, "Toggle", "Global toggle settings for every lemon module.");
    }

    private final SettingGroup sgPlaySounds = settings.createGroup("Play Sounds");
    private final SettingGroup sgVolume = settings.createGroup("Volume");

    //--------------------General--------------------//
    public final Setting<Boolean> active = sgPlaySounds.add(new BoolSetting.Builder()
        .name("Active Sound")
        .description("Play active sound when module is active.")
        .defaultValue(false)
        .build()
    );
    public final Setting<Boolean> disable = sgPlaySounds.add(new BoolSetting.Builder()
        .name("Disable Sound")
        .description("Play active sound when module is disabled.")
        .defaultValue(false)
        .build()
    );

    //--------------------Volume--------------------//
    public final Setting<Double> volume = sgVolume.add(new DoubleSetting.Builder()
        .name("Volume")
        .description("volume The volume level at which to play the sound.")
        .sliderRange(0, 1)
        .range(0, 1)
        .defaultValue(0.5)
        .build()
    );
}
