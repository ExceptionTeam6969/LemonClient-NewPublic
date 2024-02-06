package dev.lemonclient.systems.modules.player;

import dev.lemonclient.events.world.TickEvent;
import dev.lemonclient.mixin.IStatusEffectInstance;
import dev.lemonclient.settings.*;
import dev.lemonclient.systems.modules.Categories;
import dev.lemonclient.systems.modules.Module;
import dev.lemonclient.utils.Utils;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.effect.StatusEffect;

import java.util.List;

import static net.minecraft.entity.effect.StatusEffects.*;

public class PotionSpoof extends Module {
    public PotionSpoof() {
        super(Categories.Player, "Potion Spoof", "Spoofs potion statuses for you. SOME effects DO NOT work.");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Object2IntMap<StatusEffect>> spoofPotions = sgGeneral.add(new StatusEffectAmplifierMapSetting.Builder()
        .name("spoofed-potions")
        .description("Potions to add.")
        .defaultValue(Utils.createStatusEffectMap())
        .build()
    );

    private final Setting<Boolean> clearEffects = sgGeneral.add(new BoolSetting.Builder()
        .name("clear-effects")
        .description("Clears effects on module disable.")
        .defaultValue(true)
        .build()
    );

    private final Setting<List<StatusEffect>> antiPotion = sgGeneral.add(new StatusEffectListSetting.Builder()
        .name("blocked-potions")
        .description("Potions to block.")
        .defaultValue(
            LEVITATION,
            JUMP_BOOST,
            SLOW_FALLING,
            DOLPHINS_GRACE
        )
        .build()
    );

    public final Setting<Boolean> applyGravity = sgGeneral.add(new BoolSetting.Builder()
        .name("gravity")
        .description("Applies gravity when levitating.")
        .defaultValue(false)
        .build()
    );

    @Override
    public void onDeactivate() {
        if (!clearEffects.get() || !Utils.canUpdate()) return;

        for (StatusEffect effect : spoofPotions.get().keySet()) {
            if (spoofPotions.get().getInt(effect) <= 0) continue;
            if (mc.player.hasStatusEffect(effect)) mc.player.removeStatusEffect(effect);
        }
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        for (StatusEffect statusEffect : spoofPotions.get().keySet()) {
            int level = spoofPotions.get().getInt(statusEffect);
            if (level <= 0) continue;

            if (mc.player.hasStatusEffect(statusEffect)) {
                net.minecraft.entity.effect.StatusEffectInstance instance = mc.player.getStatusEffect(statusEffect);
                ((IStatusEffectInstance) instance).setAmplifier(level - 1);
                if (instance.getDuration() < 20) ((IStatusEffectInstance) instance).setDuration(20);
            } else {
                mc.player.addStatusEffect(new net.minecraft.entity.effect.StatusEffectInstance(statusEffect, 20, level - 1));
            }
        }
    }

    public boolean shouldBlock(StatusEffect effect) {
        return isActive() && antiPotion.get().contains(effect);
    }
}
