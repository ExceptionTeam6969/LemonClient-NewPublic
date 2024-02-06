package dev.lemonclient.systems.modules.movement;

import dev.lemonclient.settings.*;
import dev.lemonclient.systems.modules.Categories;
import dev.lemonclient.systems.modules.Module;
import net.minecraft.block.Block;

import java.util.List;

public class Slippy extends Module {
    public Slippy() {
        super(Categories.Movement, "Slippy", "Changes the base friction level of blocks.");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    public final Setting<Double> friction = sgGeneral.add(new DoubleSetting.Builder()
        .name("friction")
        .description("The base friction level.")
        .range(0.01, 1.10)
        .sliderRange(0.01, 1.10)
        .defaultValue(1)
        .build()
    );

    public final Setting<ListMode> listMode = sgGeneral.add(new EnumSetting.Builder<ListMode>()
        .name("list-mode")
        .description("The mode to select blocks.")
        .defaultValue(ListMode.Blacklist)
        .build()
    );

    public final Setting<List<Block>> ignoredBlocks = sgGeneral.add(new BlockListSetting.Builder()
        .name("ignored-blocks")
        .description("Decide which blocks not to slip on")
        .visible(() -> listMode.get() == ListMode.Blacklist)
        .build()
    );

    public final Setting<List<Block>> allowedBlocks = sgGeneral.add(new BlockListSetting.Builder()
        .name("allowed-blocks")
        .description("Decide which blocks to slip on")
        .visible(() -> listMode.get() == ListMode.Whitelist)
        .build()
    );

    public enum ListMode {
        Whitelist,
        Blacklist
    }
}
