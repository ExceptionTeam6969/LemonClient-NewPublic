package dev.lemonclient.systems.modules.player;

import dev.lemonclient.events.world.TickEvent;
import dev.lemonclient.mixininterface.IClientPlayerInteractionManager;
import dev.lemonclient.settings.BoolSetting;
import dev.lemonclient.settings.IntSetting;
import dev.lemonclient.settings.Setting;
import dev.lemonclient.settings.SettingGroup;
import dev.lemonclient.systems.modules.Categories;
import dev.lemonclient.systems.modules.Module;
import dev.lemonclient.utils.timers.TimerUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;

public class HandSync extends Module {
    public HandSync() {
        super(Categories.Player, "Hand Sync", "Synchronize your inventory from time to time.");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> delaySync = sgGeneral.add(new BoolSetting.Builder()
        .name("With Delay")
        .description("Use delayed sync.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Integer> delay = sgGeneral.add(new IntSetting.Builder()
        .name("Delay")
        .description("Sync delay.")
        .defaultValue(35)
        .min(0)
        .sliderRange(0, 2000)
        .visible(delaySync::get)
        .build()
    );

    private final TimerUtils timer = new TimerUtils();

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.player.isUsingItem() || (timer.passedMs(delay.get()) && delaySync.get())) {
            sendPacket(new UpdateSelectedSlotC2SPacket(mc.player.getInventory().selectedSlot));
            ((IClientPlayerInteractionManager) mc.interactionManager).syncSelected();
            timer.reset();
        }
    }
}
