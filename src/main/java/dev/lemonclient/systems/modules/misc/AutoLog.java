package dev.lemonclient.systems.modules.misc;

import dev.lemonclient.LemonClient;
import dev.lemonclient.events.world.TickEvent;
import dev.lemonclient.settings.BoolSetting;
import dev.lemonclient.settings.IntSetting;
import dev.lemonclient.settings.Setting;
import dev.lemonclient.settings.SettingGroup;
import dev.lemonclient.systems.friends.Friends;
import dev.lemonclient.systems.modules.Categories;
import dev.lemonclient.systems.modules.Module;
import dev.lemonclient.utils.Utils;
import dev.lemonclient.utils.player.DamageUtils;
import dev.lemonclient.utils.player.InvUtils;
import dev.lemonclient.utils.player.PlayerUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.DisconnectS2CPacket;
import net.minecraft.text.Text;

public class AutoLog extends Module {
    public AutoLog() {
        super(Categories.Combat, "Auto Log", "Automatically disconnects you when certain requirements are met.");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Integer> health = sgGeneral.add(new IntSetting.Builder()
        .name("health")
        .description("Automatically disconnects when health is lower or equal to this value.")
        .defaultValue(6)
        .range(0, 19)
        .sliderMax(19)
        .build()
    );
    private final Setting<Boolean> smart = sgGeneral.add(new BoolSetting.Builder()
        .name("smart")
        .description("Disconnects when you're about to take enough damage to kill you.")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> onlyTrusted = sgGeneral.add(new BoolSetting.Builder()
        .name("only-trusted")
        .description("Disconnects when a player not on your friends list appears in render distance.")
        .defaultValue(false)
        .build()
    );
    private final Setting<Boolean> totems = sgGeneral.add(new BoolSetting.Builder()
        .name("Totems")
        .description("Disconnects if you don't have totems.")
        .defaultValue(true)
        .build()
    );
    private final Setting<Integer> totemCount = sgGeneral.add(new IntSetting.Builder()
        .name("Count")
        .description("How many totems need to disconnect.")
        .defaultValue(1)
        .sliderRange(0, 36)
        .visible(totems::get)
        .build()
    );
    private final Setting<Boolean> instantDeath = sgGeneral.add(new BoolSetting.Builder()
        .name("32K")
        .description("Disconnects when a player near you can instantly kill you.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> crystalLog = sgGeneral.add(new BoolSetting.Builder()
        .name("crystal-nearby")
        .description("Disconnects when a crystal appears near you.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Integer> range = sgGeneral.add(new IntSetting.Builder()
        .name("range")
        .description("How close a crystal has to be to you before you disconnect.")
        .defaultValue(4)
        .range(1, 10)
        .sliderMax(5)
        .visible(crystalLog::get)
        .build()
    );

    private final Setting<Boolean> smartToggle = sgGeneral.add(new BoolSetting.Builder()
        .name("smart-toggle")
        .description("Disables Auto Log after a low-health logout. WILL re-enable once you heal.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> toggleOff = sgGeneral.add(new BoolSetting.Builder()
        .name("toggle-off")
        .description("Disables Auto Log after usage.")
        .defaultValue(true)
        .build()
    );

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (totems.get() && InvUtils.find(Items.TOTEM_OF_UNDYING).count() <= totemCount.get()) {
            disconnect("I do not have the totem of undying anymore, it’s time to run away :D");
            if (toggleOff.get()) this.toggle();
        }

        float playerHealth = mc.player.getHealth();
        if (playerHealth <= 0) {
            this.toggle();
            return;
        }
        if (playerHealth <= health.get()) {
            disconnect("Health was lower than " + health.get() + ".");
            if (smartToggle.get()) {
                this.toggle();
                enableHealthListener();
            }
        }

        if (smart.get() && playerHealth + mc.player.getAbsorptionAmount() - PlayerUtils.possibleHealthReductions() < health.get()) {
            disconnect("Health was going to be lower than " + health.get() + ".");
            if (toggleOff.get()) this.toggle();
        }


        if (!onlyTrusted.get() && !instantDeath.get() && !crystalLog.get()) return; // only check all entities if needed

        for (Entity entity : mc.world.getEntities()) {
            if (entity instanceof PlayerEntity && entity.getUuid() != mc.player.getUuid()) {
                if (onlyTrusted.get() && entity != mc.player && !Friends.get().isFriend((PlayerEntity) entity)) {
                    disconnect("A non-trusted player appeared in your render distance.");
                    if (toggleOff.get()) this.toggle();
                    break;
                }
                if (instantDeath.get() && PlayerUtils.isWithin(entity, 8) && DamageUtils.getSwordDamage((PlayerEntity) entity, true)
                    > playerHealth + mc.player.getAbsorptionAmount()) {
                    disconnect("Anti-32k measures.");
                    if (toggleOff.get()) this.toggle();
                    break;
                }
            }
            if (crystalLog.get() && entity instanceof EndCrystalEntity && PlayerUtils.isWithin(entity, range.get())) {
                disconnect("End Crystal appeared within specified range.");
                if (toggleOff.get()) this.toggle();
            }
        }
    }

    private void disconnect(String reason) {
        mc.player.networkHandler.onDisconnect(new DisconnectS2CPacket(Text.literal("[AutoLog] " + reason)));
    }

    private class StaticListener {
        @EventHandler
        private void healthListener(TickEvent.Post event) {
            if (isActive()) disableHealthListener();

            else if (Utils.canUpdate()
                && !mc.player.isDead()
                && mc.player.getHealth() > health.get()) {
                toggle();
                disableHealthListener();
            }
        }
    }

    private final StaticListener staticListener = new StaticListener();

    private void enableHealthListener() {
        LemonClient.EVENT_BUS.subscribe(staticListener);
    }

    private void disableHealthListener() {
        LemonClient.EVENT_BUS.unsubscribe(staticListener);
    }
}
