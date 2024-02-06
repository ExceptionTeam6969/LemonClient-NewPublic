package dev.lemonclient.systems.modules.misc;

import dev.lemonclient.settings.BoolSetting;
import dev.lemonclient.settings.Setting;
import dev.lemonclient.settings.SettingGroup;
import dev.lemonclient.systems.modules.Categories;
import dev.lemonclient.systems.modules.Module;

public class AntiPacketKick extends Module {
    public AntiPacketKick() {
        super(Categories.Misc, "Anti Packet Kick", "Attempts to prevent you from being disconnected by large packets.");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    public final Setting<Boolean> catchExceptions = sgGeneral.add(new BoolSetting.Builder()
        .name("Catch Exceptions")
        .description("Drops corrupted packets.")
        .defaultValue(false)
        .build()
    );

    public final Setting<Boolean> logExceptions = sgGeneral.add(new BoolSetting.Builder()
        .name("Log Exceptions")
        .description("Logs caught exceptions.")
        .defaultValue(false)
        .visible(catchExceptions::get)
        .build()
    );

    public boolean catchExceptions() {
        return isActive() && catchExceptions.get();
    }
}
