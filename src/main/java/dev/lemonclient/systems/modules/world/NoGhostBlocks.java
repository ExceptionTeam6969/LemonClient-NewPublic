package dev.lemonclient.systems.modules.world;

import dev.lemonclient.events.entity.player.BreakBlockEvent;
import dev.lemonclient.systems.modules.Categories;
import dev.lemonclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.BlockState;

public class NoGhostBlocks extends Module {
    public NoGhostBlocks() {
        super(Categories.World, "No Ghost Blocks", "Attempts to prevent ghost blocks arising from breaking blocks quickly. Especially useful with multiconnect.");
    }

    @EventHandler
    public void onBreakBlock(BreakBlockEvent event) {
        if (mc.isInSingleplayer()) return;

        event.cancel();

        BlockState blockState = mc.world.getBlockState(event.blockPos);
        blockState.getBlock().onBreak(mc.world, event.blockPos, blockState, mc.player);
    }
}
