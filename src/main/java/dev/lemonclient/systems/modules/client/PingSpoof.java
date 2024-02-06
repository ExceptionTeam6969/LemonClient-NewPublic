package dev.lemonclient.systems.modules.client;

import dev.lemonclient.settings.BoolSetting;
import dev.lemonclient.settings.IntSetting;
import dev.lemonclient.settings.Setting;
import dev.lemonclient.settings.SettingGroup;
import dev.lemonclient.systems.modules.Categories;
import dev.lemonclient.systems.modules.Module;

public class PingSpoof extends Module {
    public PingSpoof() {
        super(Categories.Misc, "Ping Spoof", "Modify your ping.");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    public final Setting<Boolean> keepAlive = sgGeneral.add(new BoolSetting.Builder()
        .name("Keep Alive")
        .description("Delays keep alive packets.")
        .defaultValue(true)
        .build()
    );
    public final Setting<Boolean> pong = sgGeneral.add(new BoolSetting.Builder()
        .name("Pong")
        .description("Delays pong packets.")
        .defaultValue(false)
        .build()
    );
    public final Setting<Integer> ping = sgGeneral.add(new IntSetting.Builder()
        .name("Ping")
        .description("Increases your ping by this much.")
        .defaultValue(69)
        .min(0)
        .sliderRange(0, 1000)
        .build()
    );
}
