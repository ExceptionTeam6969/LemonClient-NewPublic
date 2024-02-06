package dev.lemonclient.systems.modules.combat;

import dev.lemonclient.enums.RotationType;
import dev.lemonclient.events.game.OpenScreenEvent;
import dev.lemonclient.events.world.TickEvent;
import dev.lemonclient.managers.Managers;
import dev.lemonclient.systems.modules.Categories;
import dev.lemonclient.systems.modules.Module;
import dev.lemonclient.utils.SettingUtils;
import dev.lemonclient.utils.player.FindItemResult;
import dev.lemonclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.AnvilBlock;
import net.minecraft.block.Block;
import net.minecraft.client.gui.screen.ingame.AnvilScreen;
import net.minecraft.util.math.BlockPos;

import java.util.Objects;

public class SelfAnvil extends Module {
    public SelfAnvil() {
        super(Categories.Combat, "Self Anvil", "Automatically places an anvil on you to prevent other players from going into your hole.");
    }

    @EventHandler
    private void onOpenScreen(OpenScreenEvent event) {
        if (event.screen instanceof AnvilScreen) event.cancel();
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (place(mc.player.getBlockPos().add(0, 2, 0), InvUtils.findInHotbar(itemStack -> Block.getBlockFromItem(itemStack.getItem()) instanceof AnvilBlock))) {
            toggle();
        }
    }

    private boolean place(BlockPos blockPos, FindItemResult result) {
        if (SettingUtils.shouldRotate(RotationType.BlockPlace))
            Managers.ROTATION.start(blockPos, 0, RotationType.BlockPlace, Objects.hash(name + "placing"));
        InvUtils.swap(result.slot(), true);
        boolean place = placeBlock(blockPos, result, true);
        InvUtils.swapBack();
        if (SettingUtils.shouldRotate(RotationType.BlockPlace)) Managers.ROTATION.end(Objects.hash(name + "placing"));
        return place;
    }
}
