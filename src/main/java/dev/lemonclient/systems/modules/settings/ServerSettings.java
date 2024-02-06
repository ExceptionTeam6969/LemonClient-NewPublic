package dev.lemonclient.systems.modules.settings;

import dev.lemonclient.settings.BoolSetting;
import dev.lemonclient.settings.Setting;
import dev.lemonclient.settings.SettingGroup;
import dev.lemonclient.systems.modules.Categories;
import dev.lemonclient.systems.modules.Module;

public class ServerSettings extends Module {
    public ServerSettings() {
        super(Categories.Settings, "Server", "Global server settings for every lemon module.");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    public final Setting<Boolean> cc = sgGeneral.add(new BoolSetting.Builder()
        .name("CC Hitboxes")
        .description("Newly placed crystals require 1 block tall space without entity hitboxes.")
        .defaultValue(false)
        .build()
    );
    public final Setting<Boolean> oldVerCrystals = sgGeneral.add(new BoolSetting.Builder()
        .name("1.12.2 Crystals")
        .description("Requires 2 block tall space to place crystals.")
        .defaultValue(false)
        .build()
    );
    public final Setting<Boolean> oldVerDamage = sgGeneral.add(new BoolSetting.Builder()
        .name("1.12.2 Damage")
        .description("Calculates damages in old way.")
        .defaultValue(false)
        .build()
    );
}
