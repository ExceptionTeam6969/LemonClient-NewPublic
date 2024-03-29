package dev.lemonclient.systems.modules.player;

import dev.lemonclient.events.world.TickEvent;
import dev.lemonclient.mixin.IClientPlayerInteractionManager;
import dev.lemonclient.settings.*;
import dev.lemonclient.systems.modules.Categories;
import dev.lemonclient.systems.modules.Module;
import dev.lemonclient.utils.Utils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.util.math.BlockPos;

import java.util.List;

import static net.minecraft.entity.effect.StatusEffects.HASTE;

public class SpeedMine extends Module {
    public SpeedMine() {
        super(Categories.Player, "Speed Mine", "Allows you to quickly mine blocks.");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    public final Setting<Mode> mode = sgGeneral.add(new EnumSetting.Builder<Mode>()
        .name("Mode")
        .defaultValue(Mode.Normal)
        .onChanged(mode -> removeHaste())
        .build()
    );

    private final Setting<List<Block>> blocks = sgGeneral.add(new BlockListSetting.Builder()
        .name("Blocks")
        .description("Selected blocks.")
        .filter(block -> block.getHardness() > 0)
        .visible(() -> mode.get() != Mode.Haste)
        .build()
    );

    private final Setting<ListMode> blocksFilter = sgGeneral.add(new EnumSetting.Builder<ListMode>()
        .name("Blocks Filter")
        .description("How to use the blocks setting.")
        .defaultValue(ListMode.Blacklist)
        .visible(() -> mode.get() != Mode.Haste)
        .build()
    );

    public final Setting<Double> modifier = sgGeneral.add(new DoubleSetting.Builder()
        .name("Modifier")
        .description("Mining speed modifier. An additional value of 0.2 is equivalent to one haste level (1.2 = haste 1).")
        .defaultValue(1.4)
        .visible(() -> mode.get() == Mode.Normal)
        .min(0)
        .build()
    );

    private final Setting<Integer> hasteAmplifier = sgGeneral.add(new IntSetting.Builder()
        .name("Haste Amplifier")
        .description("What value of haste to give you. Above 2 not recommended.")
        .defaultValue(2)
        .min(1)
        .visible(() -> mode.get() == Mode.Haste)
        .onChanged(i -> removeHaste())
        .build()
    );

    private final Setting<Boolean> instamine = sgGeneral.add(new BoolSetting.Builder()
        .name("Insta Mine")
        .description("Whether or not to instantly mine blocks under certain conditions.")
        .defaultValue(true)
        .visible(() -> mode.get() == Mode.Damage)
        .build()
    );

    @Override
    public void onDeactivate() {
        removeHaste();
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (!Utils.canUpdate()) return;

        if (mode.get() == Mode.Haste) {
            StatusEffectInstance haste = mc.player.getStatusEffect(HASTE);

            if (haste == null || haste.getAmplifier() <= hasteAmplifier.get() - 1) {
                mc.player.setStatusEffect(new StatusEffectInstance(HASTE, -1, hasteAmplifier.get() - 1, false, false, false), null);
            }
        } else if (mode.get() == Mode.Damage) {
            IClientPlayerInteractionManager im = (IClientPlayerInteractionManager) mc.interactionManager;
            float progress = im.getBreakingProgress();
            BlockPos pos = im.getCurrentBreakingBlockPos();

            if (pos == null || progress <= 0) return;
            if (progress + mc.world.getBlockState(pos).calcBlockBreakingDelta(mc.player, mc.world, pos) >= 0.7f)
                im.setCurrentBreakingProgress(1f);
        }
    }

    private void removeHaste() {
        if (!Utils.canUpdate()) return;

        StatusEffectInstance haste = mc.player.getStatusEffect(HASTE);
        if (haste != null && !haste.shouldShowIcon()) mc.player.removeStatusEffect(HASTE);
    }

    public boolean filter(Block block) {
        if (blocksFilter.get() == ListMode.Blacklist && !blocks.get().contains(block)) return true;
        return blocksFilter.get() == ListMode.Whitelist && blocks.get().contains(block);
    }

    public boolean instamine() {
        return isActive() && mode.get() == Mode.Damage && instamine.get();
    }

    public enum Mode {
        Normal,
        Haste,
        Damage
    }

    public enum ListMode {
        Whitelist,
        Blacklist
    }
}
