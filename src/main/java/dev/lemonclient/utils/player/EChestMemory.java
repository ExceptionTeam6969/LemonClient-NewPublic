package dev.lemonclient.utils.player;

import dev.lemonclient.LemonClient;
import dev.lemonclient.events.game.GameLeftEvent;
import dev.lemonclient.events.game.OpenScreenEvent;
import dev.lemonclient.events.world.BlockActivateEvent;
import dev.lemonclient.utils.PreInit;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.EnderChestBlock;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.util.collection.DefaultedList;

import static dev.lemonclient.LemonClient.mc;

public class EChestMemory {
    public static final DefaultedList<ItemStack> ITEMS = DefaultedList.ofSize(27, ItemStack.EMPTY);
    private static int echestOpenedState;
    private static boolean isKnown = false;

    @PreInit
    public static void init() {
        LemonClient.EVENT_BUS.subscribe(EChestMemory.class);
    }

    @EventHandler
    private static void onBlockActivate(BlockActivateEvent event) {
        if (event.blockState.getBlock() instanceof EnderChestBlock && echestOpenedState == 0) echestOpenedState = 1;
    }

    @EventHandler
    private static void onOpenScreenEvent(OpenScreenEvent event) {
        if (echestOpenedState == 1 && event.screen instanceof GenericContainerScreen) {
            echestOpenedState = 2;
            return;
        }
        if (echestOpenedState == 0) return;

        if (!(mc.currentScreen instanceof GenericContainerScreen)) return;
        GenericContainerScreenHandler container = ((GenericContainerScreen) mc.currentScreen).getScreenHandler();
        if (container == null) return;
        Inventory inv = container.getInventory();

        for (int i = 0; i < 27; i++) {
            ITEMS.set(i, inv.getStack(i));
        }
        isKnown = true;

        echestOpenedState = 0;
    }

    @EventHandler
    private static void onLeaveEvent(GameLeftEvent event) {
        ITEMS.clear();
        isKnown = false;
    }

    public static boolean isKnown() {
        return isKnown;
    }
}
