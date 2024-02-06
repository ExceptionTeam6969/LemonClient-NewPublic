package dev.lemonclient.systems.modules.player;

import dev.lemonclient.events.entity.player.BlockBreakingCooldownEvent;
import dev.lemonclient.settings.BoolSetting;
import dev.lemonclient.settings.IntSetting;
import dev.lemonclient.settings.Setting;
import dev.lemonclient.settings.SettingGroup;
import dev.lemonclient.systems.modules.Categories;
import dev.lemonclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;

public class BreakDelay extends Module {
    public BreakDelay() {
        super(Categories.Player, "Break Delay", "Changes the delay between breaking blocks.");
    }

    SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Integer> cooldown = sgGeneral.add(new IntSetting.Builder()
        .name("cooldown")
        .description("Block break cooldown in ticks.")
        .defaultValue(0)
        .min(0)
        .sliderMax(5)
        .build()
    );

    public final Setting<Boolean> noInstaBreak = sgGeneral.add(new BoolSetting.Builder()
        .name("no-insta-break")
        .description("Prevent you from breaking blocks instantly.")
        .defaultValue(false)
        .build()
    );

    @EventHandler()
    private void onBlockBreakingCooldown(BlockBreakingCooldownEvent event) {
        event.cooldown = cooldown.get();
    }
}
